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
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.job.SearchJob;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchServlet extends AbstractSearchServlet {
	
	private static final long serialVersionUID = -7933742691498873774L;
	private static Logger searchLogger = LoggerFactory.getLogger("SEARCH_LOG");
	private static AtomicLong taskSeq = new AtomicLong();
	
	public static final int IS_ALIVE = 3;
	
    public SearchServlet(int resultType){
    	super(resultType);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request,response);
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	prepare(request);
    	
    	if(resultType == IS_ALIVE){
    		response.setContentType("text/html; charset="+responseCharset);
    		response.getWriter().write("FastCat/OK\n<br/>" + new Date());
    		return;
    	}
    	
    	String queryString = queryString();
    	
    	logger.debug("queryString = "+queryString);
    	logger.debug("timeout = "+timeout+" s");
    	
    	long seq = taskSeq.incrementAndGet();
		searchLogger.info(seq+", "+queryString);
		
    	long searchTime = 0;
    	long st = System.currentTimeMillis();
    	
    	SearchJob job = new SearchJob();
    	job.setArgs(new String[]{queryString});
    	
		ResultFuture jobResult = JobService.getInstance().offer(job);
		Object obj = jobResult.poll(timeout);
		
		searchTime = (System.currentTimeMillis() - st);
		
		ResultStringer rStringer = getResultStringer();
		
		writeHeader(response, rStringer);
		
		SearchResultWriter searchResultWriter = new SearchResultWriter(response.getWriter(), isAdmin);
		
		try {
			searchResultWriter.writeResult(obj, rStringer, searchTime, jobResult.isSuccess());
		} catch (StringifyException e) {
			logger.error("",e);
		}
		
		response.getWriter().close();
    }
}

//searchlogger
//{
//			String logStr = searchTime+", "+result.getCount()+", "+result.getTotalCount()+", "+result.getFieldCount();
//			if(result.getGroupResult() != null){
//				String grStr = ", [";
//				GroupResults aggregationResult = result.getGroupResult();
//				GroupResult[] gr = aggregationResult.groupResultList();
//				for (int i = 0; i < gr.length; i++) {
//					if(i > 0)
//						grStr += ", ";
//					grStr += gr[i].size();
//				}
//				grStr += "]";
//				logStr += grStr;
//			}
//			//searchLogger.info(seq+", "+logStr);
//			
//}