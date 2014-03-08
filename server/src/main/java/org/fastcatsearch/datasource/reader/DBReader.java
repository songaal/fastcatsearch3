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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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

import org.fastcatsearch.datasource.SourceModifier;
import org.fastcatsearch.datasource.reader.annotation.SourceReader;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.config.DataSourceConfig;
import org.fastcatsearch.ir.config.JDBCSourceInfo;
import org.fastcatsearch.ir.config.SingleSourceConfig;
import org.fastcatsearch.ir.settings.AnalyzerSetting;
import org.fastcatsearch.ir.settings.FieldIndexSetting;
import org.fastcatsearch.ir.settings.FieldSetting;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.fastcatsearch.ir.settings.GroupIndexSetting;
import org.fastcatsearch.ir.settings.IndexSetting;
import org.fastcatsearch.ir.settings.PrimaryKeySetting;
import org.fastcatsearch.ir.settings.RefSetting;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SourceReader(name="DBMS")
public class DBReader extends SingleSourceReader<Map<String, Object>> {

	private static Logger logger = LoggerFactory.getLogger(DBReader.class);
	private int BULK_SIZE;

	private Connection con;
	private PreparedStatement pstmt;
	private ResultSet r;
	private int columnCount;
	private String[] columnName;
	private Map<String, Object>[] dataSet;

	private int bulkCount;
	private int readCount;

	public DBReader(File filePath, DataSourceConfig dataSourceConfig, SingleSourceConfig singleSourceConfig, SourceModifier sourceModifier, String lastIndexTime)
			throws IRException {
		super(filePath, dataSourceConfig, singleSourceConfig, sourceModifier, lastIndexTime);
	}


	@Override
	protected void initParameters() {
		registerParameter(new SourceReaderParameter("jdbcSourceId", "JDBC ID", "Select jdbc connection", "JDBC", true, null));
		registerParameter(new SourceReaderParameter("bulkSize", "Bulk Size"
				, "DBReader reads BulkSize amount of data in advance on memory, then provides to consumer."
				, SourceReaderParameter.TYPE_NUMBER, true, "100"));
		registerParameter(new SourceReaderParameter("fetchSize", "Fetch Size"
				, "JDBC statement fetch-size. if this value is 0, JDBC uses read-only cursor."
				, SourceReaderParameter.TYPE_NUMBER, true, "1000"));
		registerParameter(new SourceReaderParameter("dataSQL", "Data SQL", "Query for indexing."
				, SourceReaderParameter.TYPE_TEXT, true, null));
		registerParameter(new SourceReaderParameter("deleteIdSQL", "Delete SQL", "Query for delete documents while indexing."
				, SourceReaderParameter.TYPE_TEXT, false, null));
		registerParameter(new SourceReaderParameter("beforeSQL", "Before SQL", "Query before indexing."
				, SourceReaderParameter.TYPE_TEXT, false, null));
		registerParameter(new SourceReaderParameter("afterSQL", "After SQL", "Query after indexing."
				, SourceReaderParameter.TYPE_TEXT, false, null));
	}
	
	@Override
	public void init() throws IRException {
		this.BULK_SIZE = getConfigInt("bulkSize");

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
			if (sourceModifier != null) {
				sourceModifier.init(this);
			}
			doBeforeQuery();

			String deleteIdSQL = getConfigString("deleteIdSQL");
			if (deleteIdSQL != null && deleteIdSQL.length() > 0) {
				PreparedStatement idPstmt = null;
				ResultSet rs = null;
				try {
					idPstmt = con.prepareStatement(q(deleteIdSQL));
					rs = idPstmt.executeQuery();
					while (rs.next()) {
						String ID = rs.getString(1);
						deleteIdList.add(ID);
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
			if (getConfigInt("fetchSize") <= 0){
				//in mysql, fetch data row by row 
				pstmt = con.prepareStatement(q(dataSQL), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				pstmt.setFetchSize(Integer.MIN_VALUE);
			} else {
				pstmt = con.prepareStatement(q(dataSQL));
				pstmt.setFetchSize(getConfigInt("fetchSize"));
			}

			r = pstmt.executeQuery();

			ResultSetMetaData rsMetadata = r.getMetaData();
			columnCount = rsMetadata.getColumnCount();
			columnName = new String[columnCount];
			for (int i = 0; i < columnCount; i++) {
				columnName[i] = rsMetadata.getColumnName(i + 1).toUpperCase();
				String typeName = rsMetadata.getColumnTypeName(i + 1);
				logger.info("Column-{} [{}]:[{}]", new Object[] { i + 1, columnName[i], typeName });
			}
		} catch (Exception e) {
			try {
				if (r != null)
					r.close();
			} catch (SQLException e1) {
			}

			try {
				if (pstmt != null)
					pstmt.close();
			} catch (SQLException e1) {
			}

			try {
				if (con != null && !con.isClosed())
					con.close();
			} catch (SQLException e1) {
			}

			throw new IRException(e);
		}
	}

	private Connection getConnection(JDBCSourceInfo jdbcSourceInfo) throws IRException, SQLException {
		Connection con = null;
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
				con = driver.connect(jdbcSourceInfo.getUrl(), info);
				con.setAutoCommit(true);
			}
		} else {
			throw new IRException("JDBC driver is empty!");
		}
		return con;
	}

	private String q(String query) {
		if (query!=null && lastIndexTime != null) {
			if (lastIndexTime.length() == 0) {
				// 현재시각으로 넣어준다.
				query = query.replaceAll("\\$\\{last_index_time\\}", "'" + Formatter.formatDate() + "'");
			} else {
				query = query.replaceAll("\\$\\{last_index_time\\}", "'" + lastIndexTime + "'");
			}
		}

		return query;
	}

	@Override
	public void close() throws IRException {
		logger.info("DBReader has read {} docs", totalCnt);
		try {
			doAfterQuery();
		} catch (SQLException e) {
			logger.error("After Query Error => " + e.getMessage(), e);
		}

		try {
			if (r != null)
				r.close();
		} catch (SQLException e) {
		}

		try {
			if (pstmt != null)
				pstmt.close();
		} catch (SQLException e) {
		}

		try {
			if (con != null && !con.isClosed())
				con.close();
		} catch (SQLException e) {
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

			try {
				rsMeta = r.getMetaData();
			} catch (SQLException e) {
				return;
			}
			while (r.next()) {

				Map<String, Object> keyValueMap = new HashMap<String, Object>();
				boolean hasLob = false;
				
				for (int i = 0; i < columnCount; i++) {
					int columnIdx = i + 1;
					int type = rsMeta.getColumnType(columnIdx);

					String str = r.getString(columnIdx);
					if (str == null) {
						if (type == Types.BLOB || type == Types.BINARY || type == Types.LONGVARBINARY || type == Types.VARBINARY
								|| type == Types.JAVA_OBJECT) {
							// logger.debug("Column-"+columnIdx+" is BLOB!");
							// BLOB일 경우 스트림으로 받는다.
							File f = null;
							FileOutputStream os = null;
							InputStream is = null;
							try {
								f = File.createTempFile("blob." + columnIdx, ".tmp");
								// logger.debug("tmp file = "+f.getAbsolutePath());
								is = r.getBinaryStream(columnIdx);
								if (is != null) {
									os = new FileOutputStream(f);
									for (int rlen = 0; (rlen = is.read(data, 0, data.length)) != -1;) {
										os.write(data, 0, rlen);
									}

									keyValueMap.put(columnName[i], f);
									hasLob = true;
								} else {
									keyValueMap.put(columnName[i], "");
								}

							} catch (IOException e) {
								throw new IRException("Error while writing Blob field. column => " + rsMeta.getColumnName(columnIdx));
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
									logger.error("Error while close blob field and output file stream.", ex);
								}
							}
						} else if (type == Types.CLOB) {

							File f = null;
							BufferedWriter os = null;
							BufferedReader is = null;
							try {
								f = File.createTempFile("clob." + columnIdx, ".tmp");
								Reader reader = r.getCharacterStream(columnIdx);
								if (reader != null) {
									is = new BufferedReader(reader);
									os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
									while ((str = is.readLine()) != null) {
										os.write(str);
									}
									keyValueMap.put(columnName[i], f);
									hasLob = true;
								} else {
									keyValueMap.put(columnName[i], "");
								}
							} catch (IOException e) {
								throw new IRException("Error while writing Clob field. column => " + rsMeta.getColumnName(columnIdx));
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
								if (f != null && f.exists()) {
									f.delete();
								}
								if (ex != null) {
									logger.error("Error while close clob field and output file stream.", ex);
								}
							}
						} else {
							// java 1.6 이상지원 jdbc4.0 CLOB 필드
							// CLOB과 동일하게 처리해준다.
							// java1.5의 경우는 위에서 처리가 되며,
							// 데이터가 null이거나 java1.6이상의 CLOB데이터는 이곳으로 넘어오게 되는데, 에러가 발생하면 null로 처리해준다.
							try {
								if (type == Types.NCLOB || type == Types.SQLXML || type == Types.LONGVARCHAR || type == Types.LONGNVARCHAR) {
									File f = null;
									BufferedWriter os = null;
									BufferedReader is = null;
									try {
										f = File.createTempFile("clob." + columnIdx, ".tmp");
										Reader reader = r.getCharacterStream(columnIdx);
										if (reader != null) {
											is = new BufferedReader(reader);
											os = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
											while ((str = is.readLine()) != null) {
												os.write(str);
											}
											keyValueMap.put(columnName[i], f);
											hasLob = true;
										} else {
											keyValueMap.put(columnName[i], "");
										}
									} catch (IOException e) {
										throw new IRException("Error while writing Clob field. column => " + rsMeta.getColumnName(columnIdx));
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
										if (f != null && f.exists()) {
											f.delete();
										}
										if (ex != null) {
											logger.error("Error while close clob field and output file stream.", ex);
										}
									}
								}
							} catch (Error e) {
								// 에러무시.
								// java1.5에서 데이터가 실제 null인 경우이므로 무시한다.
							}

							// 파싱할 수 없는 자료형 이거나 정말 NULL 값인 경우
							keyValueMap.put(columnName[i], "");
						}
					} else {
						keyValueMap.put(columnName[i], str.trim());
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
				if (r != null)
					r.close();
			} catch (SQLException e1) {
			}

			try {
				if (pstmt != null)
					pstmt.close();
			} catch (SQLException e1) {
			}

			try {
				if (con != null && !con.isClosed())
					con.close();
			} catch (SQLException e1) {
			}

			throw new IRException(e);
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
				List<RefSetting> primaryFieldList = new ArrayList<RefSetting>();
				List<FieldSetting> fieldSettingList = new ArrayList<FieldSetting>();
				List<AnalyzerSetting> analyzerSetting = new ArrayList<AnalyzerSetting>();
				List<GroupIndexSetting> groupIndexSetting = new ArrayList<GroupIndexSetting>();
				List<IndexSetting> indexSetting = new ArrayList<IndexSetting>();
				List<FieldIndexSetting> fieldIndexSetting = new ArrayList<FieldIndexSetting>();
				
				logger.trace("columnCount:{}", meta.getColumnCount());
				
				String tableName = null;
				
				for(int inx=0;inx<meta.getColumnCount(); inx++) {
					if(tableName == null) {
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
						size = 4;
						break;
					case Types.BIGINT:
						type = Type.LONG;
						size = 8;
						break;
					case Types.FLOAT:
						type = Type.FLOAT;
						size = 4;
						break;
					case Types.DOUBLE:
						type = Type.DOUBLE;
						size = 8;
						break;
					case Types.DATE:
					case Types.TIME:
					case Types.TIMESTAMP:
						type = Type.DATETIME;
						size = 8;
						break;
					case Types.CHAR:
					case Types.VARCHAR:
					case Types.LONGVARCHAR:
						type = Type.STRING;
						size = 0;
						break;
					default:
						type = Type.STRING;
						size = 0;
						break;
					}
					field.setId(meta.getColumnLabel(inx + 1));
					field.setName(field.getId());
					field.setType(type);
					field.setSize(size);
					logger.trace("field add {}", field);
					fieldSettingList.add(field);
				}
				res.close();
				DatabaseMetaData dm = con.getMetaData( );
				res = dm.getPrimaryKeys( "" , "" , tableName );
				meta = res.getMetaData();
				if(logger.isTraceEnabled()) {
					for (int inx = 0; inx < meta.getColumnCount(); inx++) {
						logger.trace("get meta :{}", meta.getColumnName(inx + 1));
					}
				}
				while (res.next()) {
					String pkey = res.getString("COLUMN_NAME").toUpperCase();
					for(int inx=0;inx < fieldSettingList.size(); inx++) {
						//logger.trace("matching pk column:{}:{}", pkey, fieldSettingList.get(inx).getId());
						if(fieldSettingList.get(inx).getId().equals(pkey)) {
							RefSetting ref = new RefSetting();
							ref.setRef(fieldSettingList.get(inx).getId());
							primaryFieldList.add(ref);
							break;
						}
					}
				}
				res.close();
				
				primaryKeySetting.setFieldList(primaryFieldList);
				setting.setFieldSettingList(fieldSettingList);
				setting.setPrimaryKeySetting(primaryKeySetting);
				setting.setFieldIndexSettingList(fieldIndexSetting);
				setting.setAnalyzerSettingList(analyzerSetting);
				setting.setGroupIndexSettingList(groupIndexSetting);
				setting.setIndexSettingList(indexSetting);
				
				con.getMetaData();
				return setting;
			}
		} catch (IRException e) {
			logger.error("",e);
		} catch (SQLException e) {
			logger.error("",e);
		} finally {
			if(res!=null) try {
				res.close();
			} catch (SQLException ignore){}
			if(pst!=null) try {
				pst.close();
			} catch (SQLException ignore){}
			if(con!=null) try {
				con.close();
			} catch (SQLException ignore){}
		}
		return null;
	}
}
