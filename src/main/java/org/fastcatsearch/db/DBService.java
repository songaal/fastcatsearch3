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

package org.fastcatsearch.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.common.DynamicClassLoader;
import org.fastcatsearch.db.dao.DAOBase;
import org.fastcatsearch.db.dao.IndexingHistory;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.db.dao.IndexingSchedule;
import org.fastcatsearch.db.dao.JobHistory;
import org.fastcatsearch.db.dao.MapDictionary;
import org.fastcatsearch.db.dao.SearchEvent;
import org.fastcatsearch.db.dao.SearchMonitoringInfo;
import org.fastcatsearch.db.dao.SearchMonitoringInfoMinute;
import org.fastcatsearch.db.dao.SetDictionary;
import org.fastcatsearch.db.dao.SystemMonitoringInfo;
import org.fastcatsearch.db.dao.SystemMonitoringInfoMinute;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.config.IRConfig;
//import org.fastcatsearch.keyword.KeywordFail;
//import org.fastcatsearch.keyword.KeywordHit;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.PluginSetting.DAO;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.IRSettings;
import org.fastcatsearch.settings.Settings;

public class DBService extends AbstractService {

	public final static String DB_NAME = "db";
	private String JDBC_URL;

	protected static DBService instance;

	private Map<String, DAOBase> daoMap;

	private ConnectionManager connectionManager;

	public static DBService getInstance() {
		return instance;
	}

	public void asSingleton() {
		instance = this;
	}

	public DBService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	protected boolean doStart() throws ServiceException {
		JDBC_URL = "jdbc:derby:" + IRSettings.path(DB_NAME);

		IRConfig config = IRSettings.getConfig();
		String dbhandlerType = config.getString("dbhandler.type");

		if ("external".equals(dbhandlerType)) {
			String cls = config.getString("dbhandler.external.jdbccls");
			String url = config.getString("dbhandler.external.jdbcurl");
			String user = config.getString("dbhandler.external.jdbcuser");
			String pass = config.getString("dbhandler.external.jdbcpass");
			logger.info(cls + ":" + url + ":" + user + ":" + pass);
			try {
				Class.forName(cls).newInstance();
			} catch (Exception e) {
				throw new ServiceException("Cannot load driver class!", e);
			}

			try {
				connectionManager = new ConnectionManager(url, user, pass);
				// conn = DriverManager.getConnection(url, user, pass);
				// 2012-10-16
				// 오토커밋으로 변경.
				// conn.setAutoCommit(false);
				logger.info("DBHandler create and init DB!");
				this.testAndInitDB();
				// conn.commit();
			} catch (SQLException e) {
				// conn = createDB(url,user,pass);
				throw new ServiceException("관리DB로의 연결을 생성할수 없습니다.", e);
			}
		} else {
			try {
				// Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
				DynamicClassLoader.loadClass("org.apache.derby.jdbc.EmbeddedDriver");
			} catch (Exception e) {
				throw new ServiceException("Cannot load driver class!", e);
			}

			// try {
			connectionManager = new ConnectionManager(JDBC_URL, null, null);
			// conn = DriverManager.getConnection(JDBC_URL);
			// } catch (SQLException e) {
			// logger.info("DBHandler create and init DB!");
			// // if DB is not created.
			// conn = createDB(JDBC_URL, null, null);
			// if (conn == null) {
			// throw new ServiceException("내부 DB로의 연결을 생성할수 없습니다. DB를 이미 사용중인 프로세스가 있는지 확인필요.", e);
			// }
			// }

			try {
				// 오토커밋으로 변경.
				initDB();
			} catch (SQLException e1) {
				throw new ServiceException(e1);
			}

			try {
				testAndInitDB();
			} catch (SQLException e) {
				throw new ServiceException(e);
			}

			try {
				initMONDB();
			} catch (SQLException e1) {
				throw new ServiceException(e1);
			}

			try {
				testAndInitMONDB();
			} catch (SQLException e) {
				throw new ServiceException(e);
			}
		}

		logger.info("DBHandler started!");
		return true;
	}

	private void initDB() throws SQLException {

		daoMap = new HashMap<String, DAOBase>();

		// 기본DAO
		daoMap.put("IndexingResult", new IndexingResult(connectionManager));
		daoMap.put("IndexingSchedule", new IndexingSchedule(connectionManager));
		daoMap.put("IndexingHistory", new IndexingHistory(connectionManager));
		daoMap.put("JobHistory", new JobHistory(connectionManager));
		daoMap.put("SearchEvent", new SearchEvent(connectionManager));
		daoMap.put("SearchMonitoringInfoMinute", new SearchMonitoringInfoMinute(connectionManager));
		daoMap.put("SearchMonitoringInfo", new SearchMonitoringInfo(connectionManager));
		daoMap.put("SystemMonitoringInfoMinute", new SystemMonitoringInfoMinute(connectionManager));
		daoMap.put("SystemMonitoringInfo", new SystemMonitoringInfo(connectionManager));

		// 사전추가. xml설정에서 읽어온다.
		daoMap.put("RecommendKeyword", new MapDictionary("RecommendKeyword", connectionManager));
		// 사전.
		daoMap.put("SynonymDictionary", new SetDictionary("SynonymDictionary", connectionManager));
		daoMap.put("UserDictionary", new SetDictionary("UserDictionary", connectionManager));
		daoMap.put("StopDictionary", new SetDictionary("StopDictionary", connectionManager));

		// pluginDaoMap.put("KeywordHit", new KeywordHit(connectionManager));
		// pluginDaoMap.put("KeywordFail", new KeywordFail(connectionManager));

		// 플러그인 DAO
		PluginService pluginService = serviceManager.getService(PluginService.class);
		Collection<Plugin> plugins = pluginService.getPlugins();
		logger.debug("plugin 로딩. plugins size={}", plugins.size());
		for (Plugin plugin : plugins) {
			PluginSetting pluginSetting = plugin.getPluginSetting();
			String pluginId = pluginSetting.getId();
			if (pluginSetting.getDB() != null) {
				List<DAO> daoList = pluginSetting.getDB().getDAOList();
				logger.debug("plugin db daoList 로딩. daoList.size={}", daoList.size());
				for (int i = 0; i < daoList.size(); i++) {
					DAO dao = daoList.get(i);
					String daoName = dao.getName();
					String daoId = pluginId + daoName;
					//
					// TODO dao id가 기존것과 서로 중복될수 있다.
					// plugin은 plugin을 붙인다.
					//
//					String daoKey = getPluginDaoKey(pluginId, daoId);
					String className = dao.getClassName();
					DAOBase daoBase = DynamicClassLoader.loadObject(className, DAOBase.class,
							new Class<?>[] { ConnectionManager.class }, new Object[] { connectionManager });
					if (daoBase == null) {
						// daoId 를 tableName 으로 전달한다.
						daoBase = DynamicClassLoader.loadObject(className, DAOBase.class, new Class<?>[] { String.class,
								ConnectionManager.class }, new Object[] { daoId, connectionManager });
					}

					if (daoBase != null) {
						daoMap.put(daoId, daoBase);
						logger.debug("register plugin dao {} >> {}", daoId, className);
					}
				}
			}
		}

	}

	private String getPluginDaoKey(String pluginId, String daoId) {
		return pluginId + "@" + daoId;
	}

	public <T> T getDAO(String daoId) {
		return (T) daoMap.get(daoId);
	}

	public <T> T getDAO(String daoId, Class<T> clazz) {
		return (T) daoMap.get(daoId);
	}

	public <T> T getPluginDAO(String pluginId, String daoId, Class<T> clazz) {
		return (T) daoMap.get(getPluginDaoKey(pluginId, daoId));
	}

	private void testAndInitDB() throws SQLException {
		Iterator<DAOBase> iterator = daoMap.values().iterator();
		while (iterator.hasNext()) {
			((DAOBase) iterator.next()).testAndCreate();
		}

		((IndexingResult) getDAO("IndexingResult")).repairStatus();
	}

	private void initMONDB() throws SQLException {
		// RecommendKeyword = new RecommendKeyword();
		// SystemMonitoringInfoMinute = new SystemMonitoringInfoMinute();
		// SystemMonInfoHDWMY = new SystemMonitoringInfo();
		//
		// RecommendKeyword.setConnectionManager(connectionManager);
		// SystemMonitoringInfoMinute.setConnectionManager(connectionManager);
		// SystemMonInfoHDWMY.setConnectionManager(connectionManager);

	}

	private void testAndInitMONDB() throws SQLException {
		// RecommendKeyword.testAndCreate();
		// SystemMonitoringInfoMinute.testAndCreate();
		// SystemMonInfoHDWMY.testAndCreate();
		//
		// SystemMonitoringInfoMinute.prepareID();
		// SystemMonInfoHDWMY.prepareID();

	}

	// private Connection createDB(String jdbcurl, String jdbcuser, String jdbcpass) {
	// try {
	// logger.info("Creating Fastcat DB = " + jdbcurl);
	// if (jdbcuser != null && jdbcpass != null) {
	// return DriverManager.getConnection(jdbcurl + ";create=true", jdbcuser, jdbcpass);
	// } else {
	// return DriverManager.getConnection(jdbcurl + ";create=true");
	// }
	// } catch (SQLException e) {
	//
	// }
	// return null;
	// }

	protected boolean doStop() throws ServiceException {
		try {
			logger.info("DBHandler shutdown! " + connectionManager);
			connectionManager.close();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	/**
	 * DAO를 통하지 않고 임시 SQL문등을 실행(jsp페이지등)할때 db커넥션등의 리소스를 관리해주는 객체. 
	 * */
	public DBContext getDBContext(){
		try {
			return new DBContext(connectionManager);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
//
//	// mon db
//	public int updateOrInsertSQLMONDB(String sql) throws SQLException {
//		Connection conn = getConn();
//		try {
//			Statement stmt = conn.createStatement();
//			int n = stmt.executeUpdate(sql);
//			stmt.close();
//			return n;
//		} finally {
//			if (conn != null) {
//				conn.close();
//			}
//		}
//	}
//
//	public ResultSet selectSQLMONDB(String sql) throws SQLException {
//		Connection conn = getConn();
//		try {
//			Statement stmt = conn.createStatement();
//			ResultSet rs = stmt.executeQuery(sql);
//			stmt.close();
//			return rs;
//		} finally {
//			if (conn != null) {
//				conn.close();
//			}
//		}
//	}

	@Override
	protected boolean doClose() throws ServiceException {
		return true;
	}

}
