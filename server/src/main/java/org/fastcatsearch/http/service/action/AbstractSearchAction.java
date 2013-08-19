package org.fastcatsearch.http.service.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionRequest;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.SearchJob;
import org.fastcatsearch.servlet.AbstractSearchResultWriter;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSearchAction extends ServiceAction {

	protected static AtomicLong taskSeq = new AtomicLong();
	protected static Logger searchLogger = LoggerFactory.getLogger("SEARCH_LOG");

	public static final int DEFAULT_TIMEOUT = 5; // 5초.
	
	public AbstractSearchAction() {
	}
	
	public AbstractSearchAction(String type) {
		super(type);
	}

	protected abstract void doSearch(long requestId, String queryString, int timeout, PrintWriter response) throws Exception;

	protected abstract Job createSearchJob(String queryString);

	protected abstract AbstractSearchResultWriter createSearchResultWriter(Writer writer);

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {

		//TODO type별로.
		response.setContentType("json");
		
		String queryString = request.getQueryString();
		int timeout = request.getIntParameter("timeout");
		writeHeader(response);
		long requestId = taskSeq.incrementAndGet();

		logger.debug("queryString = {}", queryString);
		logger.debug("timeout = {} s", timeout);

		PrintWriter writer = response.getWriter();
		response.setStatus(HttpResponseStatus.OK);
		try{
			doSearch(requestId, queryString, timeout, writer);
		}finally {
			writer.close();
		}

	}
	public void writeHeader(ActionResponse response) {
		writeHeader(response, responseCharset);
	}
	protected void writeSearchLog(long requestId, Object obj, long searchTime) {
		if (obj instanceof Result) {
			Result result = (Result) obj;
			String logStr = requestId + "," + searchTime + "," + result.getCount() + "," + result.getTotalCount();
			searchLogger.info(logStr);
		}
	}
	
	protected ResultStringer getSearchResultStringer(){
		return getResultStringer("fastcatsearch", true, "");
	}
}
