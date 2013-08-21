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

package org.fastcatsearch.log;

import java.sql.Timestamp;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.SearchEvent;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;


public class EventDBAppender extends AppenderBase<LoggingEvent> {
	
	public static int INDEX_LOG_CODE = 0;
	public static int SEARCH_LOG_CODE = 1;
	public static String INFO_TYPE = "INFO";
	public static String WARN_TYPE = "WARN";
	public static String ERROR_TYPE = "ERROR";
	public static String INDEX_CATEGORY = "색인";
	public static String SEARCH_CATEGORY = "검색";
	
	public static String STATUS_TRUE = "T";
	public static String STATUS_FALSE = "F";
	
	public void append(LoggingEvent eventObject) {
		//error=40000 warn=30000 info=20000 debug=10000
		int level = eventObject.getLevel().levelInt;
		switch (level) {
		case 20000:
			store(eventObject, INFO_TYPE, STATUS_TRUE);
			break;
		case 30000:
			store(eventObject, WARN_TYPE);
			break;
		case 40000:
			store(eventObject, ERROR_TYPE);
			break;
		default:
			break;
		}
	}
	
	private void store(LoggingEvent eventObject, String type){
		store(eventObject, type, STATUS_FALSE);
	}
	private void store(LoggingEvent eventObject, String type, String status){
		Timestamp when = new Timestamp(eventObject.getTimeStamp());
		String stacktrace = eventObject.getMessage();
		Object[] args = eventObject.getArgumentArray();
		int cateType = ((Integer)args[0]).intValue();
		if(args[0] != null){
			String message = "";
			if (args[1] != null) {
				message = (String)args[1];
			}
			
			
			DBService.getInstance().db().getDAO("SearchEvent", SearchEvent.class).insert(when, type, cateType, message, stacktrace, status);
//			DBHandler.getInstance().commit();
		}
	}
	
}
