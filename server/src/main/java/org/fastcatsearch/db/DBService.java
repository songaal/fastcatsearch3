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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.ExceptionHistoryMapper;
import org.fastcatsearch.db.mapper.GroupAccountMapper;
import org.fastcatsearch.db.mapper.GroupAuthorityMapper;
import org.fastcatsearch.db.mapper.IndexingHistoryMapper;
import org.fastcatsearch.db.mapper.IndexingResultMapper;
import org.fastcatsearch.db.mapper.ManagedMapper;
import org.fastcatsearch.db.mapper.NotificationHistoryMapper;
import org.fastcatsearch.db.mapper.TaskHistoryMapper;
import org.fastcatsearch.db.mapper.UserAccountMapper;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class DBService extends AbstractService {


	protected static DBService instance;
	private InternalDBModule internalDBModule;

	private Class<?>[] mapperList = new Class<?>[]{
			ExceptionHistoryMapper.class
			,NotificationHistoryMapper.class
			,TaskHistoryMapper.class
			,IndexingHistoryMapper.class
			,IndexingResultMapper.class
			,GroupAccountMapper.class
			,GroupAuthorityMapper.class
			,UserAccountMapper.class
			
	};
	public static DBService getInstance() {
		return instance;
	}

	public void asSingleton(){
		instance = this;
	}
	
	public DBService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
		String dbPath = environment.filePaths().file("db/system").getAbsolutePath();
		//system관련 mapper설정.
		List<URL> mapperFileList = new ArrayList<URL>();
		for(Class<?> mapperDAO : mapperList){
			try {
				String mapperFilePath = mapperDAO.getName().replace('.', '/') +".xml";
				URL mapperFile = Resources.getResourceURL(mapperFilePath);
				mapperFileList.add(mapperFile);
			} catch (IOException e) {
				logger.error("error load defaultDictionaryMapperFile", e);
			}
		}
		internalDBModule = new InternalDBModule(dbPath, mapperFileList, environment, settings, serviceManager);
		
	}
	
	public <T> MapperSession<T> getMapperSession(Class<T> type){
		SqlSession session = internalDBModule.openSession();
		return new MapperSession<T>(session, session.getMapper(type));
		
	}
	
	protected boolean doStart() throws FastcatSearchException {
		internalDBModule.load();
		for(Class<?> mapperDAO : mapperList){
			Class<ManagedMapper> clazz = (Class<ManagedMapper>) mapperDAO;
			MapperSession<ManagedMapper> mapperSession = (MapperSession<ManagedMapper>) getMapperSession(clazz);
			ManagedMapper managedMapper = mapperSession.getMapper();
			try{
				logger.debug("valiadte {}", clazz.getSimpleName());
				managedMapper.validateTable();
			}catch(Exception e){
				try{
					logger.debug("drop {}", clazz.getSimpleName());
					managedMapper.dropTable();
					mapperSession.commit();
				}catch(Exception ignore){
				}
				try{
					logger.debug("create table {}", clazz.getSimpleName());
					managedMapper.createTable();
					mapperSession.commit();
					logger.debug("create index {}", clazz.getSimpleName());
					managedMapper.createIndex();
					mapperSession.commit();
				}catch(Exception e2){
					logger.error("", e2);
				}
			}
			mapperSession.closeSession();
			
		}
		
		
		
		logger.info("DBService started!");
		return true;
	}


	protected boolean doStop() throws FastcatSearchException {
		try {
			logger.info("DBService shutdown! ");
			internalDBModule.unload();
		} catch (ModuleException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}


	@Override
	protected boolean doClose() throws FastcatSearchException {
		internalDBModule = null;
		return true;
	}

}
