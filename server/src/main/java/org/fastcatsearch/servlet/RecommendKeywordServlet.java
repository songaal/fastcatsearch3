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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.SetDictionaryDAObak;
import org.fastcatsearch.db.vo.SetDictionaryVO;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;

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
    	List<SetDictionaryVO> recommendList = dbHandler.db().getDAO("RecommendKeyword", SetDictionaryDAObak.class).selectWithExactKeyword(keyword.trim());
    	List<String> termList = null;
    	String mainWord = "";
    	if(recommendList != null && recommendList.size() > 0){
    		SetDictionaryVO recommendKeyword = recommendList.get(0);
    		String listStr = recommendKeyword.keyword;
    		if(listStr != null){
    			termList = new ArrayList<String>();
    			String[] list = listStr.split(",");
    			if(list != null){
    		    	for (String word : list) {
    		    		word = word.trim();
    		    		if(word.startsWith("@")){
    		    			mainWord = word;
    		    		}else{
    		    			termList.add(word);
    		    		}
    				}
    			}
    		}
    	}
		String responseCharset = getParameter(request, "responseCharset", "UTF-8");
    	String jsonCallback = request.getParameter("jsoncallback");
		
    	ResponseWriter rStringer = super.getResultStringer("recommend-keyword", true, jsonCallback);
    	
    	try {
			rStringer.object()
				.key("keyword").value(mainWord);
			
	    	if(termList != null){
	    		rStringer.key("list").array("item");
		    	for (String word : termList) {
		    		rStringer.value(word);
				}
		    	rStringer.endArray();
	    	}
	    	rStringer.endObject();
		} catch (ResultWriterException e) {
    		logger.error("exception",e);
    		throw new IOException(e.toString());
		}
    	
		writeHeader(response, rStringer, responseCharset);
		
    	PrintWriter writer = response.getWriter();
    	
    	writer.write(rStringer.toString());
    	
    	if( writer !=null ) { writer.close(); }
    }
}