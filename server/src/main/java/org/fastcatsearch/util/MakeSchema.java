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

package org.fastcatsearch.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.fastcatsearch.datasource.DataSourceSetting;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.settings.IRSettings;
import org.fastcatsearch.util.DynamicClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MakeSchema {
	private static Logger logger = LoggerFactory.getLogger(MakeSchema.class);
	private static String strDelimeter = "\"";
	private static String strSchemaHead = "<schema";
	private static String strSchemaTail = "</schema>";
	private static String strFieldHead = "<field";
	private static String strSchematail = "/>";
	private static String strName = "name=";
	private static String strType = "type=";
	private static String strIndex = "index=\"org.fastcatsearch.ir.analysis.KoreanTokenizer\"";
	private static String strSize = "size=";
	private static String strPrimary = "primary=";

	public static boolean makeSchema(String collection) throws IRException, SQLException {
		String strHead = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

		Connection con = null;
		PreparedStatement pstmt = null;
		// 여기서는 index 까지는 만들지 않는다.
		StringBuffer sb = new StringBuffer();
		FileOutputStream writer = null;
		ResultSet r = null;

		DataSourceSetting setting = IRSettings.getDatasource(collection, true);
		if ( "db".equalsIgnoreCase(setting.sourceType) == false )
			return false;

		try {
			if (setting.driver != null && setting.driver.length() > 0) {
				Object object = DynamicClassLoader.loadObject(setting.driver);
				if (object == null) {
					throw new IRException("Cannot find sql driver = " + setting.driver);
				} else {
					Driver driver = (Driver) object;
					DriverManager.registerDriver(driver);
					Properties info = new Properties();
					info.put("user", setting.user);
					info.put("password", setting.password);
					logger.debug("url : " + setting.url);
					logger.debug("info : " +  info.toString());
					con = driver.connect(setting.url, info);
					con.setAutoCommit(true);
				}
			} else
				throw new IRException("JDBC driver is empty!");

			if (setting.fullQuery == null || setting.fullQuery.length() == 0)
				throw new IRException("Full query sql is empty!");

			pstmt = con.prepareStatement(setting.fullQuery);

			if (setting.fetchSize > 0)
				pstmt.setFetchSize(setting.fetchSize);
			else if (setting.fetchSize <= 0)
				pstmt.setFetchSize(Integer.MIN_VALUE);

			r = pstmt.executeQuery();

			ResultSetMetaData rsMetadata = r.getMetaData();
			int columnCount = rsMetadata.getColumnCount();

			sb.append(strHead).append("\r\n");
			sb.append(strSchemaHead).append(" ").append(strName).append(strDelimeter).append(collection).append(strDelimeter);
			sb.append(" ").append("version=\"1.0\">").append("\r\n");

			for (int i = 0; i < columnCount; i++)
				sb.append(makeField(rsMetadata, i + 1));
			sb.append(strSchemaTail);

			try {
				writer = new FileOutputStream(IRSettings.getKey(collection, IRSettings.schemaWorkFilename));
				writer.write(sb.toString().getBytes());
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				// ignore
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
			}
		}
		return false;

	}

	private static String makeField(ResultSetMetaData rsMetadata, int idx) {
		String strTypeValue = "";
		String strSizeValue = "";

		StringBuffer sb = new StringBuffer();

		try {

			switch (rsMetadata.getColumnType(idx)) {
			case Types.INTEGER:
			case Types.TINYINT:
			case Types.SMALLINT:
				strTypeValue = "int";
				strSizeValue = "4";
				break;

			case Types.BIGINT:
				strTypeValue = "long";
				strSizeValue = "8";
				break;

			case Types.NUMERIC:
			case Types.DOUBLE:
			case Types.FLOAT:
				strTypeValue = "double";
				strSizeValue = "8";
				break;

			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				strTypeValue = "datetime";
				strSizeValue = "8";
				break;

			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
				strTypeValue = "uchar";
				strSizeValue = rsMetadata.getColumnDisplaySize(idx) > 256 ? "-1" : rsMetadata.getColumnDisplaySize(idx) + "";
				break;

			default:
				strTypeValue = "achar";
				strSizeValue = rsMetadata.getColumnDisplaySize(idx) > 256 ? "-1" : rsMetadata.getColumnDisplaySize(idx) + "";
				break;
			}

			// field
			sb.append(strFieldHead).append(" ");
			// Name
			sb.append(strName).append(strDelimeter).append(rsMetadata.getColumnName(idx).toLowerCase()).append(strDelimeter).append(" ");
			if (1 == idx) {
				// 첫번째 필드는 primary로 설정한다.
				// primary일때 uchar를 achar로 변환한다.
				if (strTypeValue.equalsIgnoreCase("uchar")) {
					strTypeValue = "achar";
				}
				// type
				sb.append(strType).append(strDelimeter).append(strTypeValue).append(strDelimeter).append(" ");
				// primary
				sb.append(strPrimary).append(strDelimeter).append("true").append(strDelimeter).append(" ");
			} else
				// type
				sb.append(strType).append(strDelimeter).append(strTypeValue).append(strDelimeter).append(" ");
			
			if ( idx > 1 && strTypeValue.equalsIgnoreCase("uchar"))
				sb.append(strIndex).append(" ");
			
			// size
			sb.append(strSize).append(strDelimeter).append(strSizeValue).append(strDelimeter).append(" ");
			// tail
			sb.append(strSchematail).append("\r\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

}
