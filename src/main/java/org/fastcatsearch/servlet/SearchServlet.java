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
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.FieldSetting;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.field.ScoreField;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.io.AsciiCharTrie;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.util.Formatter;
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
    	
    	Result result = null;
    	
		ResultFuture jobResult = JobService.getInstance().offer(job);
		Object obj = jobResult.poll(timeout);
		
		ResultStringer rStringer = getResultStringer();
		
		SearchResultWriter searchResultWriter = new SearchResultWriter(response.getWriter(), responseCharset, isAdmin);
		
		searchResultWriter.writeResult(result, rStringer, searchTime, jobResult.isSuccess());
		
		response.getWriter().close();
		
		//끝.
		
    }

}