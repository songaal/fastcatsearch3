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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.fastcatsearch.db.dao.DAOBase;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.module.AbstractModule;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.util.DynamicClassLoader;

public class InternalDBModule extends AbstractModule {

	private final static String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";

	private final String dbName;

	protected static InternalDBModule instance;

	private Map<String, DAOBase> daoMap;

	private ConnectionManager connectionManager;

	public static InternalDBModule getInstance() {
		return instance;
	}

	public void asSingleton() {
		instance = this;
	}

	public InternalDBModule(String dbName, Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings);
		this.dbName = dbName;
	}

	@Override
	protected boolean doLoad() throws ModuleException {
		String jdbcUrl = "jdbc:derby:" + environment.filePaths().path("db", dbName).toString();

		try {
			DynamicClassLoader.loadClass(derbyDriver);
		} catch (Exception e) {
			throw new ModuleException(e);
		}

		// try {
		connectionManager = new ConnectionManager(jdbcUrl, null, null);
		try {
			connectionManager.releaseConnection(connectionManager.getConnection());
		} catch (SQLException e) {
			throw new ModuleException(e);
		}

		daoMap = new HashMap<String, DAOBase>();
		logger.info("DBModule[{}] Loaded!", dbName);
		return true;
	}

	@Override
	protected boolean doUnload() throws ModuleException {
		try {
			logger.info("DBModule[{}] Unloaded!, conn = {}", dbName, connectionManager);
			connectionManager.close();
			daoMap.clear();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	
	public void addDAO(String daoName, DAOBase daoBase) throws SQLException {

		try {
			daoBase.setConnectionManager(connectionManager);
			daoBase.prepare();
			daoMap.put(daoName, daoBase);
		} catch (SQLException e) {
			throw new ModuleException(e);
		}
		
		
//		daoMap = new HashMap<String, DAOBase>();

		// 기본DAO
//		daoMap.put("IndexingResult", new IndexingResult(connectionManager));
//		daoMap.put("IndexingSchedule", new IndexingSchedule(connectionManager));
//		daoMap.put("IndexingHistory", new IndexingHistory(connectionManager));
//		daoMap.put("JobHistory", new JobHistory(connectionManager));
//		daoMap.put("SearchEvent", new SearchEvent(connectionManager));
//		daoMap.put("SearchMonitoringInfoMinute", new SearchMonitoringInfoMinute(connectionManager));
//		daoMap.put("SearchMonitoringInfo", new SearchMonitoringInfo(connectionManager));
//		daoMap.put("SystemMonitoringInfoMinute", new SystemMonitoringInfoMinute(connectionManager));
//		daoMap.put("SystemMonitoringInfo", new SystemMonitoringInfo(connectionManager));
//
//		daoMap.put("RecommendKeyword", new SetDictionary("RecommendKeyword", connectionManager));
//		// 사전.
//		daoMap.put("SynonymDictionary", new SetDictionary("SynonymDictionary", connectionManager));
//		daoMap.put("UserDictionary", new SetDictionary("UserDictionary", connectionManager));
//		daoMap.put("StopDictionary", new SetDictionary("StopDictionary", connectionManager));

		// pluginDaoMap.put("KeywordHit", new KeywordHit(connectionManager));
		// pluginDaoMap.put("KeywordFail", new KeywordFail(connectionManager));

		// 플러그인 DAO
		// PluginService pluginService = serviceManager.getService(PluginService.class);
		// Collection<Plugin> plugins = pluginService.getPlugins();
		// logger.debug("plugin 로딩. plugins size={}", plugins.size());
		// for (Plugin plugin : plugins) {
		// PluginSetting pluginSetting = plugin.getPluginSetting();
		// String pluginId = pluginSetting.getId();
		//
		//
		//
		// if (pluginSetting.getDB() != null) {
		// List<DAO> daoList = pluginSetting.getDB().getDAOList();
		// logger.debug("plugin db daoList 로딩. daoList.size={}", daoList.size());
		// for (int i = 0; i < daoList.size(); i++) {
		// DAO dao = daoList.get(i);
		// String daoName = dao.getName();
		// String daoId = pluginId + daoName;
		// //
		// // TODO dao id가 기존것과 서로 중복될수 있다.
		// // plugin은 plugin을 붙인다.
		// //
		// // String daoKey = getPluginDaoKey(pluginId, daoId);
		// String className = dao.getClassName();
		// DAOBase daoBase = DynamicClassLoader.loadObject(className, DAOBase.class,
		// new Class<?>[] { ConnectionManager.class }, new Object[] { connectionManager });
		// if (daoBase == null) {
		// // daoId 를 tableName 으로 전달한다.
		// daoBase = DynamicClassLoader.loadObject(className, DAOBase.class, new Class<?>[] { String.class,
		// ConnectionManager.class }, new Object[] { daoId, connectionManager });
		// }
		//
		// if (daoBase != null) {
		// daoMap.put(daoId, daoBase);
		// logger.debug("register plugin dao {} >> {}", daoId, className);
		// }
		// }
		// }
		// }

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
			((DAOBase) iterator.next()).prepare();
		}

		((IndexingResult) getDAO("IndexingResult")).repairStatus();
	}

	/**
	 * DAO를 통하지 않고 임시 SQL문등을 실행(jsp페이지등)할때 db커넥션등의 리소스를 관리해주는 객체.
	 * */
	public DBContext getDBContext() {
		try {
			return new DBContext(connectionManager);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

}
