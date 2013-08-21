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

package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.SearchMonitoringInfoMinute;
import org.fastcatsearch.db.dao.SearchMonitoringInfo;
import org.fastcatsearch.db.vo.SearchMonitoringInfoMinuteVO;
import org.fastcatsearch.db.vo.SearchMonitoringInfoVO;
import org.json.JSONException;
import org.json.JSONStringer;


/**
 * 데이터는 mondb에서 가져와서 응답해준다.
 * @author swsong
 *
 */
public class SearchMonServlet extends WebServiceHttpServlet {
	
	private static final long serialVersionUID = 963640566656747847L;
	private DBService handler = DBService.getInstance();
	
	public SearchMonServlet() {
	}
	
    public SearchMonServlet(int resultType){
    	super(resultType);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
    Random r = new Random(System.currentTimeMillis());
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String q = request.getParameter("q");
    	String test = request.getParameter("test");
    	
    	String start_0 = request.getParameter("start_0");
    	String end_0 = request.getParameter("end_0");
    	String start_1 = request.getParameter("start_1");
    	String end_1 = request.getParameter("end_1");
    	String start_2 = request.getParameter("start_2");
    	String end_2 = request.getParameter("end_2");
    	
    	if ("".equals(start_0)) {
    		start_0 = null;
		}
    	if ("".equals(start_1)) {
    		start_1 = null;
		}
    	if ("".equals(start_2)) {
    		start_2 = null;
		}
    	if ("".equals(end_0)) {
    		end_0 = null;
		}
    	if ("".equals(end_1)) {
    		end_1 = null;
		}
    	if ("".equals(end_2)) {
    		end_2 = null;
		}
    	
    	String type = request.getParameter("type");
    	String collection = request.getParameter("collection");
    	boolean isTest = false;
    	if(test != null && test.equals("true")){
    		isTest = true;
    	}
    	
    	response.setCharacterEncoding("utf-8");
    	PrintWriter w = response.getWriter();
    	if(q != null){
    		//지원사항을 알려준다.
    		//TODO:누적 모니터링 사용가능 체크 
    	}else{
    		String callback = request.getParameter("jsoncallback");
        	response.setStatus(HttpServletResponse.SC_OK);
        	
    		if(resultType == JSONP_TYPE) {
    			response.setContentType("text/javascript;");
    			
    		}else{
    			response.setContentType("application/json;");
    		}
    		
    		String result = "";
    		try {
	    			if(isTest){
	    				//결과생성
	    	    		JSONStringer stringer = new JSONStringer();
	    				stringer.array();
	    				for (int i = 0; i < 60; i++) {
	    					stringer.object()
	    					.key("id").value(i)
			    			.key("hit").value(r.nextInt(60))
			    			.key("fail").value(r.nextInt(60))
			    			.key("achit").value(r.nextInt(60))
			    			.key("acfail").value(r.nextInt(60))
			    			.key("ave").value(r.nextInt(60))
			    			.key("max").value(r.nextInt(60))
			    			.endObject();
						}
	    				stringer.endArray();
	    				result = stringer.toString();
	    			}else{
	    				result = pollData(start_0, end_0, start_1, end_1, start_2, end_2, collection, type);
	    			}
//    			logger.debug("stringer = "+stringer);
    		} catch (JSONException e) {
    			throw new ServletException("JSONException 발생",e);
    		}
    		
    		//JSONP는 데이터 앞뒤로 function명으로 감싸준다.
    		if(resultType == JSONP_TYPE) {
        		w.write(callback+"(");
        	}
    		
    		//정보를 보내준다.
    		w.write(result);
    		
    		if(resultType == JSONP_TYPE) {
        		w.write(");");
        	}
    		
    	}
    	w.close();
    	
    }
    
    private String pollData(String start_0, String end_0, String start_1, String end_1, String start_2, String end_2, String collection, String type) throws JSONException{
    	logger.debug("idx_0 = {} ~ {}", start_0, end_0);
    	logger.debug("idx_1 = {} ~ {}", start_1, end_1);
    	logger.debug("idx_2 = {} ~ {}", start_2, end_2);
    	String re_0 = "";
    	String re_1 = "";
    	String re_2 = "";
    	
    	if (start_0 != null && end_0 != null) {
			if ("hour".equals(type)) {
				re_0 = getHour(start_0, end_0, collection);
			} else if ("day".equals(type)){
				re_0 = getDay(start_0, end_0, collection);
			} else if ("week".equals(type)){
				re_0 = getWeek(start_0, end_0, collection);
			} else if ("month".equals(type)){
				re_0 = getMonth(start_0, end_0, collection);
			}else if ("year".equals(type)){
				re_0 = getYear(start_0, end_0, collection);
			}
		} 
    	if (start_1 != null && end_1 != null) {
			if ("hour".equals(type)) {
				re_1 = getHour(start_1, end_1, collection);
			} else if ("day".equals(type)){
				re_1 = getDay(start_1, end_1, collection);
			} else if ("week".equals(type)){
				re_1 = getWeek(start_1, end_1, collection);
			} else if ("month".equals(type)){
				re_1 = getMonth(start_1, end_1, collection);
			}else if ("year".equals(type)){
				re_1 = getYear(start_1, end_1, collection);
			}
		}
    	if (start_2 != null && end_2 != null) {
			if ("hour".equals(type)) {
				re_2 = getHour(start_2, end_2, collection);
			} else if ("day".equals(type)){
				re_2 = getDay(start_2, end_2, collection);
			} else if ("week".equals(type)){
				re_2 = getWeek(start_2, end_2, collection);
			} else if ("month".equals(type)){
				re_2 = getMonth(start_2, end_2, collection);
			}else if ("year".equals(type)){
				re_2 = getYear(start_2, end_2, collection);
			}
		}
    	
    	JSONStringer stringer = new JSONStringer();
		stringer.object();
		if(!"".equals(re_0)){
			stringer.key("re_0").value(re_0);
		}
		if(!"".equals(re_1)){
			stringer.key("re_1").value(re_1);
		}
		if(!"".equals(re_2)){
			stringer.key("re_2").value(re_2);
		}
		stringer.endObject();
		
		return stringer.toString();
    }
    
    private String getHour(String startStr, String endStr, String collection) throws JSONException{
    	JSONStringer stringer = new JSONStringer();
		stringer.array();
		
		Timestamp start = Timestamp.valueOf(startStr);
		Timestamp end = Timestamp.valueOf(endStr);
		List<SearchMonitoringInfoMinuteVO> list =  handler.db().getDAO("SearchMonInfoMinute", SearchMonitoringInfoMinute.class).select(start, end, collection);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		
		Calendar calStart = Calendar.getInstance();
		calStart.setTime(start);
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTime(end);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp time = null;
		int index = list.size() - 1;
		
		while(calStart.compareTo(calEnd) <= 0) {
			if(index < 0){
				stringer.object()
				.key("id").value(0)
				.key("hit").value(0)
    			.key("fail").value(0)
    			.key("achit").value(0)
    			.key("acfail").value(0)
    			.key("ave").value(0)
    			.key("max").value(0)
//				.key("hit").value(r.nextInt(60))
//    			.key("fail").value(r.nextInt(60))
//    			.key("achit").value(r.nextInt(60))
//    			.key("acfail").value(r.nextInt(60))
//    			.key("ave").value(r.nextInt(60))
//    			.key("max").value(r.nextInt(60))
				.key("time").value(sdf.format(calStart.getTime()))
				.endObject();
			}else{
				SearchMonitoringInfoMinuteVO search = list.get(index);
				time = search.when;
				
				calendar.setTime(time);
				int yearCurr = calendar.get(Calendar.YEAR);
				int monCurr = calendar.get(Calendar.MONTH);
				int dayCurr = calendar.get(Calendar.DATE);
				int hourCurr = calendar.get(Calendar.HOUR_OF_DAY);
				int minuteCurr = calendar.get(Calendar.MINUTE);
				
				int yearAll = calStart.get(Calendar.YEAR);
				int monAll = calStart.get(Calendar.MONTH);
				int dayAll = calStart.get(Calendar.DATE);
				int hourAll = calStart.get(Calendar.HOUR_OF_DAY);
				int minuteAll = calStart.get(Calendar.MINUTE);
				
				if (yearAll == yearCurr && monAll == monCurr && dayAll == dayCurr && hourAll ==  hourCurr && minuteAll == minuteCurr) {
					stringer.object()
					.key("id").value(search.id)
	    			.key("hit").value(search.hit)
	    			.key("fail").value(search.fail)
	    			.key("achit").value(search.achit)
	    			.key("acfail").value(search.acfail)
	    			.key("ave").value(search.ave_time)
	    			.key("max").value(search.max_time)
					.key("time").value(sdf.format(time))
					.endObject();
					index--;
					if (index >= 0) {
						search = list.get(index);
					}
				} else {
					stringer.object()
					.key("hit").value(0)
	    			.key("fail").value(0)
	    			.key("achit").value(0)
	    			.key("acfail").value(0)
	    			.key("ave").value(0)
	    			.key("max").value(0)
//					.key("hit").value(r.nextInt(60))
//	    			.key("fail").value(r.nextInt(60))
//	    			.key("achit").value(r.nextInt(60))
//	    			.key("acfail").value(r.nextInt(60))
//	    			.key("ave").value(r.nextInt(60))
//	    			.key("max").value(r.nextInt(60))
					.key("time").value(sdf.format(calStart.getTime()))
					.endObject();
				}
			}
			calStart.set(Calendar.MINUTE, calStart.get(Calendar.MINUTE) + 1);
		}
		stringer.endArray();
		return stringer.toString();
    }
    
    private String getDay(String startStr, String endStr, String collection) throws JSONException{
    	JSONStringer stringer = new JSONStringer();
		stringer.array();
		
		Timestamp start = Timestamp.valueOf(startStr);
		Timestamp end = Timestamp.valueOf(endStr);
		List<SearchMonitoringInfoVO> list =  handler.db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).select(start, end, collection, "h");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		int year = calendar.get(Calendar.YEAR);
		int mon = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		
		Calendar calStart = Calendar.getInstance();
		calStart.set(year, mon, day, 0, 0, 0);
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(year, mon, day, 23, 59, 59);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp time = null;
		int index = list.size() - 1;
		
		while(calStart.compareTo(calEnd) <= 0) {
			if(index < 0){
				stringer.object()
				.key("hit").value(0)
    			.key("fail").value(0)
    			.key("achit").value(0)
    			.key("acfail").value(0)
    			.key("ave").value(0)
    			.key("max").value(0)
				.key("time").value(sdf.format(calStart.getTime()))
				.endObject();
			}else{
				SearchMonitoringInfoVO search = list.get(index);
				time = search.when;
				
				calendar.setTime(time);
				int yearCurr = calendar.get(Calendar.YEAR);
				int monCurr = calendar.get(Calendar.MONTH);
				int dayCurr = calendar.get(Calendar.DATE);
				int hourCurr = calendar.get(Calendar.HOUR_OF_DAY);
				
				int yearAll = calStart.get(Calendar.YEAR);
				int monAll = calStart.get(Calendar.MONTH);
				int dayAll = calStart.get(Calendar.DATE);
				int hourAll = calStart.get(Calendar.HOUR_OF_DAY);
				
				if (yearAll == yearCurr && monAll == monCurr && dayAll == dayCurr && hourAll ==  hourCurr) {
					stringer.object()
					.key("id").value(search.id)
	    			.key("hit").value(search.hit)
	    			.key("fail").value(search.fail)
	    			.key("achit").value(search.achit)
	    			.key("acfail").value(search.acfail)
	    			.key("ave").value(search.ave_time)
	    			.key("max").value(search.max_time)
					.key("time").value(sdf.format(time))
					.endObject();
					index--;
					if (index >= 0) {
						search = list.get(index);
					}
				} else {
					stringer.object()
					.key("id").value(0)
					.key("hit").value(0)
	    			.key("fail").value(0)
	    			.key("achit").value(0)
	    			.key("acfail").value(0)
	    			.key("ave").value(0)
	    			.key("max").value(0)
					.key("time").value(sdf.format(calStart.getTime()))
					.endObject();
				}
			}
			calStart.set(Calendar.HOUR_OF_DAY, calStart.get(Calendar.HOUR_OF_DAY) + 1);
		}
		stringer.endArray();
		return stringer.toString();
    }
    
    private String getWeek(String startStr, String endStr, String collection) throws JSONException{
    	JSONStringer stringer = new JSONStringer();
		stringer.array();
		
		Timestamp start = Timestamp.valueOf(startStr);
		Timestamp end = Timestamp.valueOf(endStr);
		List<SearchMonitoringInfoVO> list =  handler.db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).select(start, end, collection, "d");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		int year = calendar.get(Calendar.YEAR);
		int mon = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		Calendar calStart = Calendar.getInstance();
		calStart.set(year, mon, day, 0, 0, 0);
		
		calendar.setTime(end);
		year = calendar.get(Calendar.YEAR);
		mon = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DATE);
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(year, mon, day, 23, 59, 59);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp time = null;
		int index = list.size() - 1;
		
		while(calStart.compareTo(calEnd) <= 0) {
			if(index < 0){
				stringer.object()
				.key("id").value(0)
				.key("hit").value(0)
    			.key("fail").value(0)
    			.key("achit").value(0)
    			.key("acfail").value(0)
    			.key("ave").value(0)
    			.key("max").value(0)
//				.key("hit").value(r.nextInt(60))
//    			.key("fail").value(r.nextInt(60))
//    			.key("achit").value(r.nextInt(60))
//    			.key("acfail").value(r.nextInt(60))
//    			.key("ave").value(r.nextInt(60))
//    			.key("max").value(r.nextInt(60))
				.key("time").value(sdf.format(calStart.getTime()))
				.endObject();
			}else{
				SearchMonitoringInfoVO search = list.get(index);
				time = search.when;
				
				calendar.setTime(time);
				int yearCurr = calendar.get(Calendar.YEAR);
				int monCurr = calendar.get(Calendar.MONTH);
				int dayCurr = calendar.get(Calendar.DATE);
				
				int yearAll = calStart.get(Calendar.YEAR);
				int monAll = calStart.get(Calendar.MONTH);
				int dayAll = calStart.get(Calendar.DATE);
				
				if (yearAll == yearCurr && monAll == monCurr && dayAll == dayCurr) {
					stringer.object()
					.key("id").value(search.id)
	    			.key("hit").value(search.hit)
	    			.key("fail").value(search.fail)
	    			.key("achit").value(search.achit)
	    			.key("acfail").value(search.acfail)
	    			.key("ave").value(search.ave_time)
	    			.key("max").value(search.max_time)
					.key("time").value(sdf.format(time))
					.endObject();
					index--;
					if (index >= 0) {
						search = list.get(index);
					}
				} else {
					stringer.object()
					.key("id").value(0)
					.key("hit").value(0)
	    			.key("fail").value(0)
	    			.key("achit").value(0)
	    			.key("acfail").value(0)
	    			.key("ave").value(0)
	    			.key("max").value(0)
//					.key("hit").value(r.nextInt(60))
//	    			.key("fail").value(r.nextInt(60))
//	    			.key("achit").value(r.nextInt(60))
//	    			.key("acfail").value(r.nextInt(60))
//	    			.key("ave").value(r.nextInt(60))
//	    			.key("max").value(r.nextInt(60))
					.key("time").value(sdf.format(calStart.getTime()))
					.endObject();
				}
			}
			calStart.set(Calendar.DATE, calStart.get(Calendar.DATE) + 1);
		}
		stringer.endArray();
		return stringer.toString();
    }
    
    private String getMonth(String startStr, String endStr, String collection) throws JSONException{
    	JSONStringer stringer = new JSONStringer();
		stringer.array();
		
		Timestamp start = Timestamp.valueOf(startStr);
		Timestamp end = Timestamp.valueOf(endStr);
		List<SearchMonitoringInfoVO> list =  handler.db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).select(start, end, collection, "d");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		int year = calendar.get(Calendar.YEAR);
		int mon = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		Calendar calStart = Calendar.getInstance();
		calStart.set(year, mon, day, 0, 0, 0);
		
		calendar.setTime(end);
		year = calendar.get(Calendar.YEAR);
		mon = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DATE);
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(year, mon, day, 23, 59, 59);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp time = null;
		int index = list.size() - 1;
		
		while(calStart.compareTo(calEnd) <= 0) {
			if(index < 0){
				stringer.object()
				.key("id").value(0)
				.key("hit").value(0)
    			.key("fail").value(0)
    			.key("achit").value(0)
    			.key("acfail").value(0)
    			.key("ave").value(0)
    			.key("max").value(0)
//				.key("hit").value(r.nextInt(60))
//    			.key("fail").value(r.nextInt(60))
//    			.key("achit").value(r.nextInt(60))
//    			.key("acfail").value(r.nextInt(60))
//    			.key("ave").value(r.nextInt(60))
//    			.key("max").value(r.nextInt(60))
				.key("time").value(sdf.format(calStart.getTime()))
				.endObject();
			}else{
				SearchMonitoringInfoVO search = list.get(index);
				time = search.when;
				
				calendar.setTime(time);
				int yearCurr = calendar.get(Calendar.YEAR);
				int monCurr = calendar.get(Calendar.MONTH);
				int dayCurr = calendar.get(Calendar.DATE);
				
				int yearAll = calStart.get(Calendar.YEAR);
				int monAll = calStart.get(Calendar.MONTH);
				int dayAll = calStart.get(Calendar.DATE);
				
				if (yearAll == yearCurr && monAll == monCurr && dayAll == dayCurr) {
					stringer.object()
					.key("id").value(search.id)
	    			.key("hit").value(search.hit)
	    			.key("fail").value(search.fail)
	    			.key("achit").value(search.achit)
	    			.key("acfail").value(search.acfail)
	    			.key("ave").value(search.ave_time)
	    			.key("max").value(search.max_time)
					.key("time").value(sdf.format(time))
					.endObject();
					index--;
					if (index >= 0) {
						search = list.get(index);
					}
				} else {
					stringer.object()
					.key("id").value(0)
					.key("hit").value(0)
	    			.key("fail").value(0)
	    			.key("achit").value(0)
	    			.key("acfail").value(0)
	    			.key("ave").value(0)
	    			.key("max").value(0)
//					.key("hit").value(r.nextInt(60))
//	    			.key("fail").value(r.nextInt(60))
//	    			.key("achit").value(r.nextInt(60))
//	    			.key("acfail").value(r.nextInt(60))
//	    			.key("ave").value(r.nextInt(60))
//	    			.key("max").value(r.nextInt(60))
					.key("time").value(sdf.format(calStart.getTime()))
					.endObject();
				}
			}
			calStart.set(Calendar.DATE, calStart.get(Calendar.DATE) + 1);
		}
		stringer.endArray();
		return stringer.toString();
    }
    
    private String getYear(String startStr, String endStr, String collection) throws JSONException{
    	JSONStringer stringer = new JSONStringer();
		stringer.array();
		
		Timestamp start = Timestamp.valueOf(startStr);
		Timestamp end = Timestamp.valueOf(endStr);
		List<SearchMonitoringInfoVO> list =  handler.db().getDAO("SearchMonitoringInfo", SearchMonitoringInfo.class).select(start, end, collection, "m");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		int year = calendar.get(Calendar.YEAR);
		int mon = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		Calendar calStart = Calendar.getInstance();
		calStart.set(year, mon, day, 0, 0, 0);
		
		calendar.setTime(end);
		year = calendar.get(Calendar.YEAR);
		mon = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DATE);
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(year, mon, day, 23, 59, 59);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Timestamp time = null;
		int index = list.size() - 1;
		
		while(calStart.compareTo(calEnd) <= 0) {
			if(index < 0){
				stringer.object()
				.key("id").value(0)
				.key("hit").value(0)
    			.key("fail").value(0)
    			.key("achit").value(0)
    			.key("acfail").value(0)
    			.key("ave").value(0)
    			.key("max").value(0)
//				.key("hit").value(r.nextInt(60))
//    			.key("fail").value(r.nextInt(60))
//    			.key("achit").value(r.nextInt(60))
//    			.key("acfail").value(r.nextInt(60))
//    			.key("ave").value(r.nextInt(60))
//    			.key("max").value(r.nextInt(60))
				.key("time").value(sdf.format(calStart.getTime()))
				.endObject();
			}else{
				SearchMonitoringInfoVO search = list.get(index);
				time = search.when;
				
				calendar.setTime(time);
				int yearCurr = calendar.get(Calendar.YEAR);
				int monCurr = calendar.get(Calendar.MONTH);
				
				int yearAll = calStart.get(Calendar.YEAR);
				int monAll = calStart.get(Calendar.MONTH);
				
				if (yearAll == yearCurr && monAll == monCurr) {
					stringer.object()
					.key("id").value(search.id)
	    			.key("hit").value(search.hit)
	    			.key("fail").value(search.fail)
	    			.key("achit").value(search.achit)
	    			.key("acfail").value(search.acfail)
	    			.key("ave").value(search.ave_time)
	    			.key("max").value(search.max_time)
					.key("time").value(sdf.format(time))
					.endObject();
					index--;
					if (index >= 0) {
						search = list.get(index);
					}
				} else {
					stringer.object()
					.key("id").value(0)
					.key("hit").value(0)
	    			.key("fail").value(0)
	    			.key("achit").value(0)
	    			.key("acfail").value(0)
	    			.key("ave").value(0)
	    			.key("max").value(0)
//					.key("hit").value(r.nextInt(60))
//	    			.key("fail").value(r.nextInt(60))
//	    			.key("achit").value(r.nextInt(60))
//	    			.key("acfail").value(r.nextInt(60))
//	    			.key("ave").value(r.nextInt(60))
//	    			.key("max").value(r.nextInt(60))
					.key("time").value(sdf.format(calStart.getTime()))
					.endObject();
				}
			}
			calStart.set(Calendar.MONTH, calStart.get(Calendar.MONTH) + 1);
		}
		stringer.endArray();
		return stringer.toString();
    }
  
}
