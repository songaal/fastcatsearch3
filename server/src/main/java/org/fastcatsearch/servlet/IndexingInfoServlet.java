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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.servlet.WebServiceHttpServlet;
import org.fastcatsearch.statistics.IndexingInfo;
import org.fastcatsearch.statistics.StatisticsInfoService;
import org.json.JSONException;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author swsong
 *
 */
public class IndexingInfoServlet extends WebServiceHttpServlet {
	
	private static final long serialVersionUID = 963640595944747847L;
	private static Logger logger = LoggerFactory.getLogger(IndexingInfoServlet.class);
	
    public IndexingInfoServlet(int resultType){
    	super(resultType);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String q = request.getParameter("q");
    	String callback = request.getParameter("jsoncallback");
	
    	StatisticsInfoService statisticsInfoService = ServiceManager.getInstance().getService(StatisticsInfoService.class);
    	
		response.setCharacterEncoding("utf-8");
    	response.setStatus(HttpServletResponse.SC_OK);
    	JSONStringer stringer = new JSONStringer();
    	PrintWriter w = response.getWriter();
    	
    	if(q != null){
    		long time = 0;
    		try{
    			time = Long.parseLong(q);
    		}catch(Exception e){
    			time = System.currentTimeMillis();
    		}
    		//업데이트 여부 정보만 전송한다.
    		response.setContentType("application/json;");
			try {
    			stringer.object()
    			.key("update").value(statisticsInfoService.isIndexingInfoUpdated(time))
    			.endObject();
			} catch (JSONException e) {
				throw new ServletException("JSONException 발생",e);
			}
    	}else{
	    	if(resultType == JSONP_TYPE) {
				response.setContentType("text/javascript;");
				
			}else{
				response.setContentType("application/json;");
			}
	    	
	    	String[] collectionNameList = statisticsInfoService.getCollectionNameList();
	    	IndexingInfo[] indexInfoList = statisticsInfoService.getIndexingInfoList();
	    	if(indexInfoList == null){
	    		w.close();
	    		return;
	    	}
	    	
	    	try {
	    		stringer.array();
	    		for (int i = 0; i < collectionNameList.length; i++) {
	    			IndexingInfo indexingInfo = indexInfoList[i];
	    			stringer.object()
	    			.key("name").value(collectionNameList[i])
					.key("fdoc").value(indexingInfo.fullDoc)
					.key("finsert").value(indexingInfo.fullInsert)
	    			.key("fupdate").value(indexingInfo.fullUpdate)
	    			.key("fdelete").value(indexingInfo.fullDelete)
	    			.key("idoc").value(indexingInfo.incDoc)
	    			.key("iinsert").value(indexingInfo.incInsert)
	    			.key("iupdate").value(indexingInfo.incUpdate)
	    			.key("idelete").value(indexingInfo.incDelete)
	    			.key("tdoc").value(indexingInfo.totalDoc)
	    			.endObject();
	    		}
	    		stringer.endArray();
			} catch (JSONException e) {
				throw new ServletException("JSONException 발생",e);
			}
    	}//if(q != null)
		
		
		//JSONP는 데이터 앞뒤로 function명으로 감싸준다.
		if(resultType == JSONP_TYPE) {
    		w.write(callback+"(");
    	}
		
		//정보를 보내준다.
		w.write(stringer.toString());
		
		if(resultType == JSONP_TYPE) {
    		w.write(");");
    	}
    		
    	w.close();
    	
    }
  
}
