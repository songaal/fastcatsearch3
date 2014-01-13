package org.fastcatsearch.http.action.service;

import java.io.Writer;
import java.util.concurrent.atomic.AtomicLong;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.http.writer.AbstractSearchResultWriter;
import org.fastcatsearch.http.writer.SearchResultWriter;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.query.QueryMap;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public abstract class AbstractSearchAction extends ServiceAction {

	private static AtomicLong taskSeq = new AtomicLong();
	
	public static final int DEFAULT_TIMEOUT = 5; // 5초.

	protected abstract Job createSearchJob(QueryMap queryMap);

	protected AbstractSearchResultWriter createSearchResultWriter(Writer writer) {
		return new SearchResultWriter(getSearchResultWriter(writer));
	}

	public void doSearch(long requestId, QueryMap queryMap, int timeout, Writer writer) throws Exception {

		long searchTime = 0;
		long st = System.nanoTime();
		Job searchJob = createSearchJob(queryMap);

		ResultFuture jobResult = JobService.getInstance().offer(searchJob);
		Object obj = jobResult.poll(timeout);
		searchTime = (System.nanoTime() - st) / 1000000;

		AbstractSearchResultWriter resultWriter = createSearchResultWriter(writer);

		try {
			resultWriter.writeResult(obj, searchTime, jobResult.isSuccess());
		} catch (ResultWriterException e) {
			logger.error("", e);
		}

		writer.close();

	}

	protected long getRequestId() {
		return taskSeq.getAndIncrement();
	}

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		logger.debug("request.getParameterMap() >> {}", request.getParameterMap());
		QueryMap queryMap = new QueryMap(request.getParameterMap());
		logger.debug("queryMap tostring>> {}", queryMap);
		Integer timeout = request.getIntParameter("timeout", DEFAULT_TIMEOUT);
		String responseCharset = request.getParameter("responseCharset", DEFAULT_CHARSET);
		writeHeader(response, responseCharset);
		long requestId = getRequestId();

		logger.debug("queryMap = {}", queryMap);
		logger.debug("timeout = {} s", timeout);
		if (timeout == null) {
			timeout = DEFAULT_TIMEOUT;
		}
		Writer writer = response.getWriter();
		response.setStatus(HttpResponseStatus.OK);
		try {
			doSearch(requestId, queryMap, timeout, writer);
		} finally {
			writer.close();
		}

	}

	

	protected ResponseWriter getSearchResultWriter(Writer writer) {
		return getSearchResultWriter(writer, "_search_callback");
	}

	protected ResponseWriter getSearchResultWriter(Writer writer, String jsonCallback) {
		return getResponseWriter(writer, ServiceAction.DEFAULT_ROOT_ELEMENT, true, jsonCallback);
	}
}
