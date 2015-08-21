/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.datasource.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.JDBCSourceInfo;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.settings.AnalyzerSetting;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SourceReader(name="DBMS")
public class DBReader extends SingleSourceReader<Map<String, Object>> {

	private static final String LOB_BINARY = "LOB_BINARY";
	private static final String LOB_STRING = "LOB_STRING";
	private static Logger logger = LoggerFactory.getLogger(DBReader.class);
	private int BULK_SIZE;

	private Connection con;
	private PreparedStatement pstmt;
	private ResultSet r;
	private int columnCount;
	private String[] columnName;
	private Map<String, Object>[] dataSet;
	
	private List<File> tmpFile;

	private int bulkCount;
	private int readCount;
	
	private boolean useBlobFile;
	
	private boolean isClosed;

	public DBReader() {
		super();
	}
	
	public DBReader(String collectionId, File filePath, SingleSourceConfig singleSourceConfig, SourceModifier<Map<String, Object>> sourceModifier, String lastIndexTime)
			throws IRException {
		super(collectionId, filePath, singleSourceConfig, sourceModifier, lastIndexTime);
	}

	@Override
	protected void initParameters() {
		registerParameter(new SourceReaderParameter("jdbcSourceId", "JDBC", "Select jdbc connection", "JDBC", true, null));
		registerParameter(new SourceReaderParameter("bulkSize", "Bulk Size"
				, "DBReader reads BulkSize amount of data in advance on memory, then provides to consumer."
				, SourceReaderParameter.TYPE_NUMBER, true, "100"));
		registerParameter(new SourceReaderParameter("fetchSize", "Fetch Size"
				, "JDBC statement fetch-size. if this values is 0, the JDBC driver ignores the value and is free to make its own best guess as to what the fetch size should be. If this value is -1, the JDBC driver uses read-only cursor."
				, SourceReaderParameter.TYPE_NUMBER, true, "-1"));
		registerParameter(new SourceReaderParameter("dataSQL", "Data SQL", "Query for indexing."
				, SourceReaderParameter.TYPE_TEXT, true, null));
		registerParameter(new SourceReaderParameter("deleteIdSQL", "Delete SQL", "Query for delete documents while indexing."
				, SourceReaderParameter.TYPE_TEXT, false, null));
		registerParameter(new SourceReaderParameter("beforeSQL", "Before SQL", "Query before indexing."
				, SourceReaderParameter.TYPE_TEXT, false, null));
		registerParameter(new SourceReaderParameter("afterSQL", "After SQL", "Query after indexing."
				, SourceReaderParameter.TYPE_TEXT, false, null));
		registerParameter(new SourceReaderParameter("useBlobFile", "LOB as File", "Using *LOB ( CLOB / NCLOB / BLOB ) as File. <br/> ( You must handle it in Source-Modifier )"
				, SourceReaderParameter.TYPE_CHECK, false, "false"));
	}
	
	@Override
	public void init() throws IRException {
		
		isClosed = false;
		
		BULK_SIZE = getConfigInt("bulkSize");
		
		useBlobFile = getConfigBoolean("useBlobFile");
		
		tmpFile = new ArrayList<File>();

		dataSet = new Map[BULK_SIZE];
		String jdbcSourceId = getConfigString("jdbcSourceId");
		JDBCSourceInfo jdbcSourceInfo = null;
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		
		List<JDBCSourceInfo> jdbcSourceInfoList = irService.getJDBCSourceConfig().getJdbcSourceInfoList();
		for(JDBCSourceInfo info : jdbcSourceInfoList){
			if(info.getId().equals(jdbcSourceId)){
				jdbcSourceInfo = info;
				break;
			}
		}
		
		try {
			con = getConnection(jdbcSourceInfo);
			doBeforeQuery();

			String deleteIdSQL = getConfigString("deleteIdSQL");
			if (deleteIdSQL != null && deleteIdSQL.length() > 0) {
				PreparedStatement idPstmt = null;
				ResultSet rs = null;
				ResultSetMetaData rm = null;
				try {
					idPstmt = con.prepareStatement(q(deleteIdSQL));
					rs = idPstmt.executeQuery();
					rm = rs.getMetaData();
					while (rs.next()) {
						String[] rid = new String[rm.getColumnCount()];
						for (int inx = 0; inx < rid.length; inx++) {
							rid[inx] = rs.getString(inx+1);
						}
						deleteIdList.add(rid);
					}
				} finally {
					if(idPstmt != null){
						try{
							idPstmt.close();
						} catch (Exception e) { }
					}
					if(rs != null){
						try{
							rs.close();
						} catch (Exception e) { }
					}
				}
			}
			
			String dataSQL = getConfigString("dataSQL");
			if (dataSQL == null || dataSQL.length() == 0) {
				throw new IRException("Data query sql is empty!");
			}

			if(logger.isTraceEnabled()) {
				logger.trace("real query = {}", q(dataSQL));
			} else {
				logger.debug("Data query = {}", dataSQL);
			}
			
			int fetchSize = getConfigInt("fetchSize");
			if (fetchSize < 0){
				//in mysql, fetch data row by row 
				pstmt = con.prepareStatement(q(dataSQL), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				pstmt.setFetchSize(Integer.MIN_VALUE);
			} else {
				pstmt = con.prepareStatement(q(dataSQL));
				if (fetchSize > 0){
					pstmt.setFetchSize(fetchSize);
				}
			}

			if(maxRows > 0){
				pstmt.setMaxRows(maxRows);
			}
			
			r = pstmt.executeQuery();

			ResultSetMetaData rsMetadata = r.getMetaData();
			columnCount = rsMetadata.getColumnCount();
			columnName = new String[columnCount];
			for (int i = 0; i < columnCount; i++) {
				columnName[i] = rsMetadata.getColumnLabel(i + 1).toUpperCase();
				String typeName = rsMetadata.getColumnTypeName(i + 1);
				logger.info("Column-{} [{}]:[{}]", new Object[] { i + 1, columnName[i], typeName });
			}
		} catch (Exception e) {
			closeConnection();

			throw new IRException(e);
		}
	}

	private Connection getConnection(JDBCSourceInfo jdbcSourceInfo) throws IRException, SQLException {
		Connection con = null;
		logger.debug(">>>>>>>>>>>>>> jdbcSourceInfo > {}", jdbcSourceInfo);
		if (jdbcSourceInfo.getDriver() != null && jdbcSourceInfo.getDriver().length() > 0) {
			Object object = DynamicClassLoader.loadObject(jdbcSourceInfo.getDriver());
			if (object == null) {
				throw new IRException("Cannot find sql driver = " + jdbcSourceInfo.getDriver());
			} else {
				Driver driver = (Driver) object;
				DriverManager.registerDriver(driver);
				Properties info = new Properties();
				info.put("user", jdbcSourceInfo.getUser());
				info.put("password", jdbcSourceInfo.getPassword());
				info.put("connectTimeout", "300000");
				con = driver.connect(jdbcSourceInfo.getUrl(), info);
				con.setAutoCommit(true);
			}
		} else {
			throw new IRException("JDBC driver is empty!");
		}
		return con;
	}

	private String q(String query) {
		if (query!=null && query.length() > 0 && lastIndexTime != null) {
			if (query.indexOf("${last_index_time}") != -1) {
				if (lastIndexTime.length() == 0) {
					// 현재시각으로 넣어준다.
					query = query.replaceAll("\\$\\{last_index_time\\}", "'" + Formatter.formatDate() + "'");
				} else {
					query = query.replaceAll("\\$\\{last_index_time\\}", "'" + lastIndexTime + "'");
				}
			}
		}

		return query;
	}

	@Override
	public void close() throws IRException {
		if(!isClosed) {
			logger.info("DBReader has read {} docs", totalCnt);
			deleteTmpLob();
			try {
				doAfterQuery();
			} catch (SQLException e) {
				logger.error("After Query Error => " + e.getMessage(), e);
			}
			closeConnection();
			isClosed = true;
		}
	}
	
	private void closeConnection() throws IRException {
		
		try {
			if (r != null) {
				r.close();
			}
		} catch (SQLException ignore) {
		}

		try {
			if (pstmt != null) {
				pstmt.close();
			}
		} catch (SQLException ignore) {
		}

		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException ignore) {
		}
	}
	
	private int executeUpdateQuery(String query) throws SQLException {
		if (query == null || query.length() == 0)
			return -1;
		logger.debug("Execute Update SQL = {}", query);
		PreparedStatement pstmt = con.prepareStatement(query);
		int count = pstmt.executeUpdate();
		pstmt.close();
		return count;
	}

	private void doBeforeQuery() throws SQLException {
		int count = executeUpdateQuery(q(getConfigString("beforeSQL")));

		if (count != -1){
			logger.info("Before query updated {} rows.", count);
		}
	}

	private void doAfterQuery() throws SQLException {
		int count = executeUpdateQuery(q(getConfigString("afterSQL")));

		if (count != -1){
			logger.info("After query updated {} rows.", count);
		}
	}

	@Override
	public boolean hasNext() throws IRException {
		if (readCount >= bulkCount) {
			fill();

			if (bulkCount == 0)
				return false;

			readCount = 0;
		}
		return true;
	}

	@Override
	protected final Map<String, Object> next() throws IRException {
		if (readCount >= bulkCount) {
			fill();
			if (bulkCount == 0)
				return null;
			readCount = 0;
		}
		return dataSet[readCount++];
	}

	byte[] data = new byte[16 * 1024];
	int totalCnt = 0;

	private void fill() throws IRException {
		
		bulkCount = 0;
		try {
			ResultSetMetaData rsMeta = null;
			//이전 Tmp 데이터들을 지워준다.
			deleteTmpLob();

			try {
				rsMeta = r.getMetaData();
			} catch (SQLException e) {
				return;
			}
			while (r.next()) {

				Map<String, Object> keyValueMap = new HashMap<String, Object>();
				
				for (int i = 0; i < columnCount; i++) {
					int columnIdx = i + 1;
					int type = rsMeta.getColumnType(columnIdx);
					
					String str = "";

					String lobType = null;
					if (type == Types.BLOB || type == Types.BINARY || type == Types.LONGVARBINARY || type == Types.VARBINARY
							|| type == Types.JAVA_OBJECT) {
						lobType = LOB_BINARY;
					} else if (type == Types.CLOB || type == Types.NCLOB || type == Types.SQLXML || type == Types.LONGVARCHAR || type == Types.LONGNVARCHAR) {
						lobType = LOB_STRING;
					}
					
					if(lobType == null) {
						str = r.getString(columnIdx);
					
						if(str != null) {
							keyValueMap.put(columnName[i], str);
						} else {
							// 파싱할 수 없는 자료형 이거나 정말 NULL 값인 경우
							keyValueMap.put(columnName[i], "");
						}
					} else {
						File file = null;
						
						if(lobType == LOB_BINARY) {
							// logger.debug("Column-"+columnIdx+" is BLOB!");
							// BLOB일 경우 스트림으로 받는다.
							ByteArrayOutputStream buffer = null;
							try {
								if(!useBlobFile) {
									buffer = new ByteArrayOutputStream();
								}
								file = readTmpBlob(i, columnIdx, rsMeta, buffer);
								if(useBlobFile) {
									keyValueMap.put(columnName[i], file);
								} else {
									keyValueMap.put(columnName[i], buffer.toByteArray());
								}
							} finally {
								if (buffer != null) {
									try {
										buffer.close();
									} catch (IOException ignore) {
									}
								}
							}
						} else if(lobType == LOB_STRING) {
							StringBuilder sb = null;
							if(!useBlobFile) {
								sb = new StringBuilder();
							}
							file = readTmpClob(i, columnIdx, rsMeta, sb);
							if(useBlobFile) {
								keyValueMap.put(columnName[i], file);
							} else {
								keyValueMap.put(columnName[i], sb.toString());
							}
						}
						
						//다음 레코드 진행시 지우도록 한다.
						if(file!=null) {
							tmpFile.add(file);
						}
					}
				}

				dataSet[bulkCount] = keyValueMap;
				bulkCount++;
				totalCnt++;

				if (bulkCount >= BULK_SIZE){
					break;
				}
			}

		} catch (Exception e) {
			
			logger.debug("",e);

			try {
				if (r != null) {
					r.close();
				}
			} catch (SQLException ignore) { }

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException ignore) { }

			try {
				if (con != null && !con.isClosed()) {
					con.close();
				}
			} catch (SQLException ignore) { }

			throw new IRException(e);
		}
	}
	
	private File readTmpBlob(int columnInx, int columnNo, ResultSetMetaData rsMeta, OutputStream buffer) throws IRException, SQLException {
		File file = null;
		FileOutputStream os = null;
		InputStream is = null;
		try {
			is = r.getBinaryStream(columnNo);
			if (is != null) {
				if(buffer == null) {
					file = File.createTempFile("blob." + columnNo, ".tmp");
					os = new FileOutputStream(file);
					// logger.debug("tmp file = "+f.getAbsolutePath());
				}
				for (int rlen = 0; (rlen = is.read(data, 0, data.length)) != -1;) {
					if(buffer != null) {
						buffer.write(data, 0, rlen);
					} else {
						os.write(data, 0, rlen);
					}
				}
			}

		} catch (IOException e) {
			throw new IRException("Error while writing Blob field. column => " + rsMeta.getColumnName(columnNo));
		} finally {
			IOException ex = null;
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
					ex = e;
				}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					ex = e;
				}
			if (ex != null) {
				logger.error("Error while close LOB field and output file stream.", ex);
			}
		}
		return file;
	}

	private File readTmpClob (int columnInx, int columnNo, ResultSetMetaData rsMeta, StringBuilder buffer) throws IRException, SQLException {
		File file = null;
		BufferedWriter os = null;
		BufferedReader is = null;
		try {
			Reader reader = r.getCharacterStream(columnNo);
			if (reader != null) {
				//buffer is null when using File
				if(buffer == null) {
					file = File.createTempFile("clob." + columnNo, ".tmp");
					os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
				}
				is = new BufferedReader(reader);
				for (String rline = ""; (rline = is.readLine()) != null;) {
					if(buffer!=null) {
						buffer.append(rline).append("\n");
					} else {
						os.write(rline);
						os.write("\n");
					}
				}
			}
		} catch (IOException e) {
			throw new IRException("Error while writing Clob field. column => " + rsMeta.getColumnName(columnNo));
		} finally {
			IOException ex = null;
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					ex = e;
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					ex = e;
				}
			}
			if (ex != null) {
				logger.error("Error while close clob field and output file stream.", ex);
			}
		}
		return file;
	}
	
	private void deleteTmpLob() {
		while (tmpFile.size() > 0) {
			File file = tmpFile.remove(tmpFile.size() - 1);
			try {
				if (file.exists()) {
					FileUtils.forceDelete(file);
				}
			} catch (IOException e) {
				logger.debug("Can not delete file : {}", file.getAbsolutePath());
			}
		}
	}

	@Override
	public SchemaSetting getAutoGeneratedSchemaSetting() {
		Map<String, String> properties = singleSourceConfig.getProperties();
		String jdbcSourceId = properties.get("jdbcSourceId");
		String dataSQL = properties.get("dataSQL");
		IRService service = ServiceManager.getInstance().getService(IRService.class);
		Connection con = null;
		PreparedStatement pst = null;
		ResultSet res = null;
		ResultSetMetaData meta = null;
		try {
			JDBCSourceInfo jdbcInfo = service.getJDBCSourceInfo(jdbcSourceId);
			if (jdbcInfo != null) {
				con = getConnection(jdbcInfo);
			}
			logger.trace("get jdbc connection : {}", con);

			if (con != null) {
				logger.trace("executing sql :{}", dataSQL);
				pst = con.prepareStatement(dataSQL);
				pst.setFetchSize(1);
				pst.setMaxRows(1);
				res = pst.executeQuery();
				res.next();
				meta = res.getMetaData();

				SchemaSetting setting = new SchemaSetting();
				PrimaryKeySetting primaryKeySetting = new PrimaryKeySetting();
				List<FieldSetting> fieldSettingList = new ArrayList<FieldSetting>();
				List<AnalyzerSetting> analyzerSetting = new ArrayList<AnalyzerSetting>();
				List<GroupIndexSetting> groupIndexSetting = new ArrayList<GroupIndexSetting>();
				List<IndexSetting> indexSetting = new ArrayList<IndexSetting>();
				List<FieldIndexSetting> fieldIndexSetting = new ArrayList<FieldIndexSetting>();

				logger.trace("columnCount:{}", meta.getColumnCount());

				String tableName = null;

				for (int inx = 0; inx < meta.getColumnCount(); inx++) {
					if (tableName == null) {
						tableName = meta.getTableName(inx + 1);
					}
					FieldSetting field = new FieldSetting();
					Type type = null;
					int size = 0;
					switch (meta.getColumnType(inx + 1)) {
					case Types.INTEGER:
					case Types.TINYINT:
					case Types.SMALLINT:
					case Types.NUMERIC:
						type = Type.INT;
						break;
					case Types.BIGINT:
						type = Type.LONG;
						break;
					case Types.FLOAT:
						type = Type.FLOAT;
						break;
					case Types.DOUBLE:
						type = Type.DOUBLE;
						break;
					case Types.DATE:
					case Types.TIME:
					case Types.TIMESTAMP:
						type = Type.DATETIME;
						break;
					case Types.CHAR:
					case Types.VARCHAR:
					case Types.LONGVARCHAR:
						type = Type.STRING;
						break;
					default:
						type = Type.STRING;
						break;
					}
					field.setId(meta.getColumnLabel(inx + 1));
					field.setName(field.getId());
					field.setType(type);
					field.setSize(size);
					logger.trace("field add {}", field);
					fieldSettingList.add(field);
				}
				
				setting.setFieldSettingList(fieldSettingList);
				setting.setPrimaryKeySetting(primaryKeySetting);
				setting.setFieldIndexSettingList(fieldIndexSetting);
				setting.setAnalyzerSettingList(analyzerSetting);
				setting.setGroupIndexSettingList(groupIndexSetting);
				setting.setIndexSettingList(indexSetting);

				return setting;
			}
		} catch (IRException e) {
			logger.error("", e);
		} catch (SQLException e) {
			logger.error("", e);
		} finally {
			if (res != null)
				try {
					res.close();
				} catch (SQLException ignore) {
				}
			if (pst != null)
				try {
					pst.close();
				} catch (SQLException ignore) {
				}
			if (con != null)
				try {
					con.close();
				} catch (SQLException ignore) {
				}
		}
		return null;
	}
}
