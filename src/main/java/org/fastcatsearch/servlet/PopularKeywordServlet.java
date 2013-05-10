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
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.vo.KeywordHitVO;
import org.fastcatsearch.ir.field.DateTimeField;
//import org.fastcatsearch.keyword.KeywordHit;
import org.fastcatsearch.service.KeywordService;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;


public class PopularKeywordServlet extends WebServiceHttpServlet {
	
	private static final long serialVersionUID = -5073136564409904769L;

	public PopularKeywordServlet(int resultType){
    	super(resultType);
	}
	
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	DBService dbHandler = DBService.getInstance();
    	
    	String keyword = request.getParameter("keyword");
    	if(keyword!=null) { keyword = keyword.trim(); }
    	
		String responseCharset = getParameter(request, "responseCharset", "UTF-8");
    	String jsonCallback = request.getParameter("jsoncallback");
		
    	ResultStringer rStringer = super.getResultStringer("popular-keywords", true, jsonCallback);
    	
    	PrintWriter writer = null;
    	
    	Date date = null;
    	String q = request.getParameter("q"); //업데이트 되었는가?
    	if(q != null){
    		try {
    			rStringer.object()
    			.key("update").value("true")
	    			.endObject();
    			writer = response.getWriter();
    			writer.write(rStringer.toString());
    			writer.close();
    			return;
    			
    		} catch (StringifyException e) {
	    		logger.error("exception",e);
	    		throw new IOException(e.toString());
    		}
    	}
    	
    	String typeStr = request.getParameter("type");
    	String timeStr = request.getParameter("time");
    	String dateStr = request.getParameter("date");
    	
//    	int type = KeywordHit.POPULAR_ACCUM;
//    	int time = 0;
//    	
//    	if("time".equals(typeStr)) {
//    		type = KeywordHit.POPULAR_HOUR;
//    	} else if ("date".equals(typeStr)) {
//    		type = KeywordHit.STATISTICS_DATE;
//    	} else if ("week".equals(typeStr)) {
//    		type = KeywordHit.STATISTICS_WEEK;
//    	} else if ("month".equals(typeStr)) {
//    		type = KeywordHit.STATISTICS_MONTH;
//    	} else if ("year".equals(typeStr)) {
//    		type = KeywordHit.STATISTICS_YEAR;
//    	}
//    	
//    	if(typeStr!=null) try { type = Integer.parseInt(typeStr); } catch (NumberFormatException e) { }
//    	if(dateStr!=null && type!=KeywordHit.POPULAR_ACCUM && type!=KeywordHit.POPULAR_HOUR) {
//    		try { date = DateTimeField.parseDate(dateStr); } catch (ParseException e) { }
//    		Calendar c = Calendar.getInstance();
//    		if(date!=null) { c.setTime(date); }
//    		Date[] dates = new Date[3];
//    		int[] times = new int[2];
//    		KeywordService.calculateDate(type,c,dates,times);
//    		date = dates[0];
//    		logger.info("date info ["+date+"]");
//    	}
//    	if(timeStr!=null) { 
//    		try { time = Integer.parseInt(timeStr); } catch (NumberFormatException e) { }
//    		if(type == KeywordHit.STATISTICS_MONTH && time > 0) { time = time - 1; }
//    	} else if(date!=null) {
//    		Calendar c = Calendar.getInstance();
//    		c.setTime(date);
//    		if (type == KeywordHit.STATISTICS_DATE) {
//    			time = c.get(Calendar.DATE);
//    		} else if (type == KeywordHit.STATISTICS_WEEK) {
//				time = c.get(Calendar.DAY_OF_WEEK)-2;
//    		} else if (type == KeywordHit.STATISTICS_MONTH) {
//    			time = c.get(Calendar.MONTH) + 1;
//    		} else if (type == KeywordHit.STATISTICS_YEAR) {
//    			time = c.get(Calendar.YEAR);
//    		}
//    	}
//
//    	List<KeywordHitVO> termList = dbHandler.getDAO("KeywordHit", KeywordHit.class).selectUsingPopular(keyword, type, time, date);
//    	
//    	try {
//    		rStringer.object()
//    			.key("list").array("item");
//	    	if(termList != null && termList.size() > 0){
//    			for (int j = 0; j < termList.size(); j++) {
//    				KeywordHitVO kh = termList.get(j);
//    				rStringer.object()
//    					.key("id").value(kh.id)
//    					.key("term").value(kh.keyword)
//    					.key("hit").value(kh.hit)
//    					.key("popular").value(kh.popular)
//    					.key("prevRank").value(kh.prevRank)
//    					.key("isUsed").value(kh.isUsed)
//					.endObject();
//    			}
//	    	}
//	    	
//	    	rStringer.endArray().endObject();
//    	} catch (StringifyException e) {
//    		logger.error("exception",e);
//    		throw new IOException(e.toString());
//    	}
    	
    	writeHeader(response, rStringer, responseCharset);
    	
    	writer = response.getWriter();
    	
    	writer.write(rStringer.toString());
    	
    	if( writer !=null ) { writer.close(); }
    }
}
