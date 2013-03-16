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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.db.DBHandler;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.field.DateTimeField;
import org.fastcatsearch.keyword.KeywordHit;
import org.fastcatsearch.service.KeywordService;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PopularKeywordServlet extends HttpServlet {
	
	private static final long serialVersionUID = -5391911035578891946L;
	
	private static Logger logger = LoggerFactory.getLogger(PopularKeywordServlet.class);
	
	public static final int JSON_TYPE = 0;
	public static final int XML_TYPE = 1;
	public static final int JSONP_TYPE = 2;
	
	private int RESULT_TYPE = JSON_TYPE;
	
	public void init(){
		String type = getServletConfig().getInitParameter("result_format");
		if(type != null){
			if(type.equalsIgnoreCase("json")){
				RESULT_TYPE = JSON_TYPE;
			}else if(type.equalsIgnoreCase("xml")){
				RESULT_TYPE = XML_TYPE;
			}else if(type.equalsIgnoreCase("jsonp")){
				RESULT_TYPE = JSONP_TYPE;
			}
		}
	}
	
	public PopularKeywordServlet() {
		this(JSON_TYPE);
	}
	
	public PopularKeywordServlet(int resultType) {
		RESULT_TYPE = resultType;
	}
	
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	DBHandler dbHandler = DBHandler.getInstance();
    	
    	String keyword = request.getParameter("keyword");
    	if(keyword!=null) { keyword = keyword.trim(); }
    	String newVersion = request.getParameter("new_version");
    	
    	Date date = null;
    	String q = request.getParameter("q"); //업데이트 되었는가?
    	if(q != null){
    		long time = 0;
    		try{
    			time = Long.parseLong(q);
    		}catch(Exception e){
    			time = System.currentTimeMillis();
    		}
    		//업데이트 여부 정보만 전송한다.
    		response.setStatus(HttpServletResponse.SC_OK);
    		response.setCharacterEncoding("utf-8");
    		response.setContentType("application/json;");
    		JSONStringer stringer = new JSONStringer();
			try {
    			stringer.object()
    			.key("update").value("true")
    			.endObject();
			} catch (JSONException e) {
				throw new ServletException("JSONException 발생",e);
			}
			
			PrintWriter writer = response.getWriter();
			writer.write(stringer.toString());
			writer.close();
			return;
    	}
    	
    	String typeStr = request.getParameter("type");
    	String timeStr = request.getParameter("time");
    	String dateStr = request.getParameter("date");
    	
    	int type = KeywordHit.POPULAR_ACCUM;
    	int time = 0;
    	
    	if("time".equals(typeStr)) {
    		type = KeywordHit.POPULAR_HOUR;
    	} else if ("date".equals(typeStr)) {
    		type = KeywordHit.STATISTICS_DATE;
    	} else if ("week".equals(typeStr)) {
    		type = KeywordHit.STATISTICS_WEEK;
    	} else if ("month".equals(typeStr)) {
    		type = KeywordHit.STATISTICS_MONTH;
    	} else if ("year".equals(typeStr)) {
    		type = KeywordHit.STATISTICS_YEAR;
    	}
    	
    	if(typeStr!=null) try { type = Integer.parseInt(typeStr); } catch (NumberFormatException e) { }
    	if(dateStr!=null && type!=KeywordHit.POPULAR_ACCUM && type!=KeywordHit.POPULAR_HOUR) {
    		try { date = DateTimeField.parseDate(dateStr); } catch (ParseException e) { }
    		Calendar c = Calendar.getInstance();
    		if(date!=null) { c.setTime(date); }
    		Date[] dates = new Date[3];
    		int[] times = new int[2];
    		KeywordService.calculateDate(type,c,dates,times);
    		date = dates[0];
    		logger.info("date info ["+date+"]");
    	}
    	if(timeStr!=null) { 
    		try { time = Integer.parseInt(timeStr); } catch (NumberFormatException e) { }
    		if(type == KeywordHit.STATISTICS_MONTH && time > 0) { time = time - 1; }
    	} else if(date!=null) {
    		Calendar c = Calendar.getInstance();
    		c.setTime(date);
    		if (type == KeywordHit.STATISTICS_DATE) {
    			time = c.get(Calendar.DATE);
    		} else if (type == KeywordHit.STATISTICS_WEEK) {
				time = c.get(Calendar.DAY_OF_WEEK)-2;
    		} else if (type == KeywordHit.STATISTICS_MONTH) {
    			time = c.get(Calendar.MONTH) + 1;
    		} else if (type == KeywordHit.STATISTICS_YEAR) {
    			time = c.get(Calendar.YEAR);
    		}
    	}

    	List<KeywordHit> termList = dbHandler.KeywordHit.selectUsingPopular(keyword, type, time, date);
    	
    	PrintWriter writer = null;
    	
    	response.setCharacterEncoding("utf-8");
    	
    	if(RESULT_TYPE == XML_TYPE) {
    		response.setContentType("text/html");
    		writer = response.getWriter();
    		responseAsXml(writer, termList);
    	} else if(RESULT_TYPE == JSONP_TYPE) {
    		response.setContentType("application/json");
    		writer = response.getWriter();
    		String callback = request.getParameter("jsoncallback");
    		writer.write(callback+"(");
    		responseAsJson(writer, termList, newVersion);
    		writer.write(");");
    	} else {
    		response.setContentType("application/json");
    		writer = response.getWriter();
    		responseAsJson(writer, termList, newVersion);
    	}
    	
    	if(writer!=null) { writer.close(); }
    }
    
    public void responseAsJson(PrintWriter writer, List<KeywordHit>termList, String newVersion) throws IOException {
    	try{
    		JSONObject jobj = new JSONObject();
			
	    	if(termList != null && termList.size() > 0){
	    		if(IRSettings.getConfig().getBoolean("server.keyword.oldtype") && newVersion == null) {
	    			//과거 버젼의 인터페이스 호환을 위한 구문.
	    			//차기버젼에서는 필요없음
	    			for (int j = 0; j < termList.size(); j++) {
	    				jobj.append("list",termList.get(j));
	    			}
	    		} else {
	    			for (int j = 0; j < termList.size(); j++) {
	    				JSONObject jrow = new JSONObject();
	    				KeywordHit kh = termList.get(j);
	    				jrow.put("id", kh.id);
	    				jrow.put("term", kh.keyword);
	    				jrow.put("hit", kh.hit);
	    				jrow.put("popular", kh.popular);
	    				jrow.put("prevRank",kh.prevRank);
	    				jrow.put("isUsed", kh.isUsed);
	    				jobj.append("list", jrow);
	    			}
	    		}
	    	}
	    	
//	    	logger.debug("JSON = {}", jobj.toString());
	    	writer.write(jobj.toString());
	    	
    	}catch(JSONException e){
    		logger.error("json exception", e);
    		throw new IOException(e.toString());
    	}
    }
    
    public void responseAsXml(PrintWriter writer, List<KeywordHit>termList) throws IOException {
    	writer.append("<popularKeywords>");
    	writer.append("<keywordList>");
    	if(termList != null && termList.size() > 0){
    		for (KeywordHit kh : termList) {
    			String keyword = kh.keyword;
    			keyword = keyword.replaceAll("&", "&amp;");
    			writer.append("<keyword>");
    			writer.append("<id>").append(""+kh.id).append("</id>");
    			writer.append("<term>").append(""+keyword).append("</term>");
    			writer.append("<hit>").append(""+kh.hit).append("</hit>");
    			writer.append("<popular>").append(""+kh.popular).append("</popular>");
    			writer.append("<prevRank>").append(""+kh.prevRank).append("</prevRank>");
    			writer.append("<isUsed>").append(""+kh.isUsed).append("</isUsed>");
    			writer.append("</keyword>");
    		}
    	}
    	writer.append("</keywordList>");
    	writer.append("</popularKeywords>");
    }
}
