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
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.SearchJob;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;

public class SearchServlet extends AbstractSearchServlet {
	
	private static final long serialVersionUID = -7933742691498873774L;
	
    public SearchServlet(int resultType){
    	super(resultType);
    }
    
    protected Job createSearchJob(String queryString){
    	SearchJob job = new SearchJob();
    	job.setArgs(new String[]{queryString});
    	return job;
    }
    protected AbstractSearchResultWriter createSearchResultWriter(Writer writer){
    	return new SearchResultWriter(writer, isAdmin);
    }
    protected void doSearch(String queryString, HttpServletResponse response) throws ServletException, IOException {
    	
    	long searchTime = 0;
    	long st = System.currentTimeMillis();
    	Job searchJob = createSearchJob(queryString);
    	
		ResultFuture jobResult = JobService.getInstance().offer(searchJob);
		Object obj = jobResult.poll(timeout);
		searchTime = (System.currentTimeMillis() - st);
		
		ResultStringer rStringer = getResultStringer();
		
		writeHeader(response, rStringer);
		
		AbstractSearchResultWriter resultWriter = createSearchResultWriter(response.getWriter());
		
		try {
			resultWriter.writeResult(obj, rStringer, searchTime, jobResult.isSuccess());
		} catch (StringifyException e) {
			logger.error("",e);
		}
		
		response.getWriter().close();
		
		writeSearchLog(obj, searchTime);
		
		
    }
    
    protected void writeSearchLog(Object obj, long searchTime){
    	if(obj instanceof Result){
			Result result = (Result) obj;
			String logStr = searchTime+", "+result.getCount()+", "+result.getTotalCount()+", "+result.getFieldCount();
			if(result.getGroupResult() != null){
				String grStr = ", [";
				GroupResults groupResults = result.getGroupResult();
				GroupResult[] gr = groupResults.groupResultList();
				for (int i = 0; i < gr.length; i++) {
					if(i > 0)
						grStr += ", ";
					grStr += gr[i].size();
				}
				grStr += "]";
				logStr += grStr;
			}
			
			searchLogger.info(logStr);
		}else if(obj instanceof GroupResults){
			GroupResults groupResults = (GroupResults) obj;
			GroupResult[] gr = groupResults.groupResultList();
			String grStr = ", [";
			for (int i = 0; i < gr.length; i++) {
				if(i > 0)
					grStr += ", ";
				grStr += gr[i].size();
			}
			grStr += "]";
			searchLogger.info(grStr);
		}
    }
}
