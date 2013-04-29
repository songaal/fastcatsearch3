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
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;

public class RecommendKeywordServlet extends WebServiceHttpServlet {
	
	private static final long serialVersionUID = 5662252132538716560L;

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
		String responseCharset = getParameter(request, "responseCharset", "UTF-8");
    	String jsonCallback = request.getParameter("jsoncallback");
		
    	ResultStringer rStringer = super.getResultStringer("recommend-keyword", true, jsonCallback);
    	
    	try {
			rStringer.object()
				.key("keyword").value(keyword);
	    	if(termList != null && termList.length > 0){
	    		rStringer.key("list").array("item");
		    	for (int inx = 0; inx < termList.length; inx++) {
		    		rStringer.value(termList[inx]);
				}
		    	rStringer.endArray();
	    	}
	    	rStringer.endObject();
		} catch (StringifyException e) {
    		logger.error("exception",e);
    		throw new IOException(e.toString());
		}
    	
		writeHeader(response, rStringer, responseCharset);
		
    	PrintWriter writer = response.getWriter();
    	
    	writer.write(rStringer.toString());
    	
    	if( writer !=null ) { writer.close(); }
    }
}