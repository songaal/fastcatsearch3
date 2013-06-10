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

import java.sql.SQLException;
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
import org.fastcatsearch.db.dao.SearchEvent;
import org.fastcatsearch.db.dao.SearchMonitoringInfo;
import org.fastcatsearch.db.dao.SearchMonitoringInfoMinute;
import org.fastcatsearch.db.dao.SetDictionary;
import org.fastcatsearch.db.dao.SystemMonitoringInfo;
import org.fastcatsearch.db.dao.SystemMonitoringInfoMinute;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.plugin.Plugin;
import org.fastcatsearch.plugin.PluginService;
import org.fastcatsearch.plugin.PluginSetting;
import org.fastcatsearch.plugin.PluginSetting.DAO;
import org.fastcatsearch.service.AbstractService;
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

	public void asSingleton(){
		instance = this;
	}
	
	public DBService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	protected boolean doStart() throws FastcatSearchException {
		JDBC_URL = "jdbc:derby:" + IRSettings.path(DB_NAME);

//		IRConfig config = IRSettings.getConfig();
//		String dbhandlerType = config.getString("dbhandler.type");
//
//		if ("external".equals(dbhandlerType)) {
//			String cls = config.getString("dbhandler.external.jdbccls");
//			String url = config.getString("dbhandler.external.jdbcurl");
//			String user = config.getString("dbhandler.external.jdbcuser");
//			String pass = config.getString("dbhandler.external.jdbcpass");
//			logger.info(cls + ":" + url + ":" + user + ":" + pass);
//			try {
//				Class.forName(cls).newInstance();
//			} catch (Exception e) {
//				throw new FastcatSearchException("ERR-00315", e, cls);
//			}
//
//			try {
//				connectionManager = new ConnectionManager(url, user, pass);
//				// conn = DriverManager.getConnection(url, user, pass);
//				// 2012-10-16
//				// 오토커밋으로 변경.
//				// conn.setAutoCommit(false);
//				logger.info("DBHandler create and init DB!");
//				this.testAndInitDB();
//				// conn.commit();
//			} catch (SQLException e) {
//				// conn = createDB(url,user,pass);
//				throw new FastcatSearchException("ERR-00314", e);
//			}
//		} else {
			String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";
			try {
				DynamicClassLoader.loadClass(derbyDriver);
			} catch (Exception e) {
				throw new FastcatSearchException("ERR-00315", e, derbyDriver);
			}

			// try {
			connectionManager = new ConnectionManager(JDBC_URL, null, null);
			try {
				connectionManager.releaseConnection(connectionManager.getConnection());
			} catch (SQLException e) {
				throw new FastcatSearchException("ERR-00314", e);
			}
			
			try {
				// 오토커밋으로 변경.
				initDB();
			} catch (SQLException e) {
				throw new FastcatSearchException("ERR-00310", e);
			}

			try {
				testAndInitDB();
			} catch (SQLException e) {
				throw new FastcatSearchException("ERR-00311", e);
			}

			try {
				initMONDB();
			} catch (SQLException e) {
				throw new FastcatSearchException("ERR-00312", e);
			}

			try {
				testAndInitMONDB();
			} catch (SQLException e) {
				throw new FastcatSearchException("ERR-00313", e);
			}
//		}

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

		daoMap.put("RecommendKeyword", new SetDictionary("RecommendKeyword", connectionManager));
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
	}

	private void testAndInitMONDB() throws SQLException {
	}

	protected boolean doStop() throws FastcatSearchException {
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

	@Override
	protected boolean doClose() throws FastcatSearchException {
		return true;
	}

}
