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
		internalDBModule = new InternalDBModule("system", environment, settings, serviceManager);
		
	}

	public InternalDBModule db(){
		return internalDBModule;
	}
	protected boolean doStart() throws FastcatSearchException {
		try {
			internalDBModule.load();
			internalDBModule.addDAO("IndexingResult", new IndexingResult(null));
			internalDBModule.addDAO("IndexingSchedule", new IndexingSchedule(null));
			internalDBModule.addDAO("IndexingHistory", new IndexingHistory(null));
			internalDBModule.addDAO("JobHistory", new JobHistory(null));
			internalDBModule.addDAO("SearchEvent", new SearchEvent(null));
			internalDBModule.addDAO("SearchMonitoringInfoMinute", new SearchMonitoringInfoMinute(null));
			internalDBModule.addDAO("SearchMonitoringInfo", new SearchMonitoringInfo(null));
			internalDBModule.addDAO("SystemMonitoringInfoMinute", new SystemMonitoringInfoMinute(null));
			internalDBModule.addDAO("SystemMonitoringInfo", new SystemMonitoringInfo(null));
			internalDBModule.addDAO("RecommendKeyword", new SetDictionary("RecommendKeyword", null));
		} catch (SQLException e) {
			throw new FastcatSearchException("", e);
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
