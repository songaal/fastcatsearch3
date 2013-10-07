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
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.InternalDBModule.SessionAndMapper;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.module.ModuleException;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;

public class DBService extends AbstractService {


	protected static DBService instance;
	private InternalDBModule internalDBModule;

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
		List<File> mapperFileList = new ArrayList<File>();
		String[] mapperFilePathList = new String[]{
				"org/fastcatsearch/db/mapper/ExceptionMapper.xml"
				,"org/fastcatsearch/db/mapper/NotificationMapper.xml"
				,"org/fastcatsearch/db/mapper/TaskHistoryMapper.xml"
				,"org/fastcatsearch/db/mapper/IndexingHistoryMapper.xml"
				
		};
		
		for(String mapperFilePath : mapperFilePathList){
			try {
				File mapperFile = Resources.getResourceAsFile(mapperFilePath);
				mapperFileList.add(mapperFile);
			} catch (IOException e) {
				logger.error("error load defaultDictionaryMapperFile", e);
			}
		}
		internalDBModule = new InternalDBModule(dbPath, mapperFileList, environment, settings, serviceManager);
		
	}
	
	public <T> SessionAndMapper<T> getSessionAndMapper(Class<T> type){
		SqlSession session = internalDBModule.openSession();
		return new SessionAndMapper<T>(session, session.getMapper(type));
		
	}
	
	protected boolean doStart() throws FastcatSearchException {
		internalDBModule.load();
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
