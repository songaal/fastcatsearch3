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
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.object.RecommendKeyword;
import org.json.JSONException;
import org.json.JSONObject;


public class RecommendKeywordServlet extends WebServiceHttpServlet {
	
	public RecommendKeywordServlet() { }
	
	public RecommendKeywordServlet(int resultType){
    	super(resultType);
	}
	
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String keyword = request.getParameter("keyword");
    	if(keyword == null){
    		keyword = "";
    	}
    	keyword = URLDecoder.decode(keyword, "utf-8");
    	DBService dbHandler = DBService.getInstance();
    	RecommendKeyword recommendKeyword = dbHandler.RecommendKeyword.exactSearch(keyword.trim());
    	String[] termList = null;
    	
    	if(recommendKeyword != null){
    		String listStr = recommendKeyword.value;
    		if(listStr != null){
    			termList = listStr.split(",");
    		}
    	}
    	
    	PrintWriter writer = null;
    	
    	response.setCharacterEncoding("utf-8");
    	
    	if(resultType == XML_TYPE) {
	    	response.setContentType("text/html");
    		writer = response.getWriter();
    		responseAsXml(writer, keyword, termList);
    	} else if(resultType == JSONP_TYPE) {
	    	response.setContentType("application/json");
    		writer = response.getWriter();
    		String callback = request.getParameter("jsoncallback");
    		writer.write(callback+"(");
    		responseAsJson(writer, keyword, termList);
    		writer.write(");");
    	} else {
	    	response.setContentType("application/json");
    		writer = response.getWriter();
    		responseAsJson(writer, keyword, termList);
    	}
    	
    	if( writer !=null ) { writer.close(); }
    }
    
    public void responseAsJson(PrintWriter writer, String keyword, String[] termList) throws IOException {
    	try{
    		JSONObject jobj = new JSONObject();
    		jobj.put("keyword", keyword);
			
	    	if(termList != null && termList.length > 0){
		    	for (int j = 0; j < termList.length; j++) {
		    		jobj.append("list", termList[j]);
				}
	    	}
	    	
	    	logger.debug("JSON = "+jobj.toString());
	    	writer.write(jobj.toString());
	    	
    	}catch(JSONException e){
    		logger.error("json exception",e);
    		throw new IOException(e.toString());
    	}
    }
    
    public void responseAsXml(PrintWriter writer, String keyword, String[] termList) throws IOException {
    	writer.append("<recommendedKeyword>");
    	writer.append("<keyword>").append(keyword).append("</keyword>");
    	writer.append("<termList>");
    	if(termList != null && termList.length > 0){
    		for (int j = 0; j < termList.length; j++) {
    			String term = termList[j];
    			term = term.replaceAll("&", "&amp;");
    			writer.append("<term>").append(termList[j]).append("</term>");
    		}
    	}
    	writer.append("</termList>");
    	writer.append("</recommendedKeyword>");
    }
}
