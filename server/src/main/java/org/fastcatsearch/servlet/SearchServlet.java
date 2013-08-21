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
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.SingleSearchJob;
import org.fastcatsearch.util.ResultWriter;
import org.fastcatsearch.util.ResultWriterException;

public class SearchServlet extends AbstractSearchServlet {
	
	private static final long serialVersionUID = -7933742691498873774L;
	
    public SearchServlet(int resultType){
    	super(resultType);
    }
    
    protected Job createSearchJob(String queryString){
    	SingleSearchJob job = new SingleSearchJob();
    	job.setArgs(new String[]{queryString});
    	return job;
    }
    protected AbstractSearchResultWriter createSearchResultWriter(Writer writer){
    	return null;//new SearchResultWriter(writer, isAdmin);
    }
    
    @Override
    protected void doSearch(long requestId, String queryString, HttpServletResponse response) throws ServletException, IOException {
    	
    	long searchTime = 0;
    	long st = System.currentTimeMillis();
    	Job searchJob = createSearchJob(queryString);
    	
		ResultFuture jobResult = JobService.getInstance().offer(searchJob);
		Object obj = jobResult.poll(timeout);
		searchTime = (System.currentTimeMillis() - st);
		writeSearchLog(requestId, obj, searchTime);
		
		ResultWriter rStringer = getResultStringer();
		writeHeader(response, rStringer);
		
		AbstractSearchResultWriter resultWriter = createSearchResultWriter(response.getWriter());
		
//		try {
//			resultWriter.writeResult(obj, rStringer, searchTime, jobResult.isSuccess());
//		} catch (StringifyException e) {
//			logger.error("",e);
//		}
		
		response.getWriter().close();
		
    }
}
