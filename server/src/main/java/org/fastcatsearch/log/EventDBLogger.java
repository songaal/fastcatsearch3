///*
// * Copyright (c) 2013 Websquared, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the GNU Public License v2.0
// * which accompanies this distribution, and is available at
// * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
// * 
// * Contributors:
// *     swsong - initial API and implementation
// */
//
//package org.fastcatsearch.log;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class EventDBLogger {
//	private static Logger logger = LoggerFactory.getLogger("EVENT_LOG");
//	private static int MAX_VARCHAR_SIZE = 3000;
//	
//	private static String[] CATE_NAME = {
//		"색인",
//		"검색",
//		"관리"
//		};
//	public static int CATE_INDEX = 0; //색인
//	public static int CATE_SEARCH = 1; //검색	
//	public static int CATE_MANAGEMENT = 2; //	
//	
//	public static String getStackTrace(Throwable e){
//		StringWriter sw = new StringWriter();
//		PrintWriter pw = new PrintWriter(sw);
//		e.printStackTrace(pw);
//		String stackTrace = sw.toString();
//		pw.close();
//		return stackTrace;
//	}
//	
//	public static void info(int category, String summary){
//		logger.info("[EMPTY]", category, summary);
//	}
//	
//	public static void warn(int category, String summary){
//		logger.warn("[EMPTY]", category, summary);
//	}
//
//	public static void error(int category, String summary){
//		logger.error("[EMPTY]", category, summary);
//	}
//	
//	public static void info(int category, String summary, String stacktrace){
//		stacktrace = checkTraceSize(stacktrace);
//		logger.info(stacktrace, category, summary);
//	}
//	
//	public static void warn(int category, String summary, String stacktrace){
//		stacktrace = checkTraceSize(stacktrace);
//		logger.warn(stacktrace, category, summary);
//	}
//
//	public static void error(int category, String summary, String stacktrace){
//		stacktrace = checkTraceSize(stacktrace);
//		logger.error(stacktrace, category, summary);
//	}
//	
//	public static String getCateName(int cateType) {
//		return CATE_NAME[cateType];
//	}
//	
//	private static String checkTraceSize(String str){
//		if(str.length() > MAX_VARCHAR_SIZE){
//			return str.substring(0, MAX_VARCHAR_SIZE);
//		}else{
//			return str;
//		}
//	}
//}
//
