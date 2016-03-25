package org.fastcatsearch.http.action.service;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.error.SearchError;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.http.writer.AbstractSearchResultWriter;
import org.fastcatsearch.http.writer.SearchResultWriter;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.query.QueryMap;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.concurrent.atomic.AtomicLong;

public abstract class AbstractSearchAction extends ServiceAction {
	
	private static final Logger requestLogger = LoggerFactory.getLogger("REQUEST_LOG");

	private static final Logger searchErrorLogger = LoggerFactory.getLogger("SEARCH_ERROR_LOG");

	private static AtomicLong taskSeq = new AtomicLong();
	
	public static final int DEFAULT_TIMEOUT = 5; // 5초.

	protected abstract Job createSearchJob(QueryMap queryMap);

	protected AbstractSearchResultWriter createSearchResultWriter(Writer writer, boolean isFieldLowercase, boolean noUnicode) {
		return new SearchResultWriter(getSearchResultWriter(writer, isFieldLowercase, noUnicode));
	}

	public Object doSearch(long requestId, QueryMap queryMap, int timeout, Writer writer) throws Exception {

		long searchTime = 0;
		long st = System.nanoTime();
		Job searchJob = createSearchJob(queryMap);
        searchJob.setTimeout(timeout * 1000L);
		ResultFuture jobResult = JobService.getInstance().offer(searchJob);
		Object obj = jobResult.poll(timeout);
		searchTime = (System.nanoTime() - st) / 1000000;

        // searchOption에서 lowercase가 존재하는지 확인후, 존재하면 lowercase로 결과를 기록하도록 한다.
        String so = queryMap.get(Query.EL.so.name());
        Metadata meta = new Metadata();
        meta.setSearchOptions(so);
        boolean isFieldLowercase = meta.isSearchOption(Query.SEARCH_OPT_LOWERCASE);
        boolean noUnicode = meta.isSearchOption(Query.SEARCH_OPT_NOUNICODE);
        AbstractSearchResultWriter resultWriter = createSearchResultWriter(writer, isFieldLowercase, noUnicode);

		try {
			resultWriter.writeResult(obj, searchTime, jobResult.isSuccess());
		} catch (ResultWriterException e) {
			logger.error("", e);
		}

//		writer.close();

		return obj;
	}

	protected long getRequestId() {
		return taskSeq.getAndIncrement();
	}

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		long requestId = getRequestId();
		requestLogger.info("request id:{} uri:{} param:{}", requestId, request.uri(), request.getParameterString());
		logger.debug("request.getParameterMap() >> {}", request.getParameterMap());
		QueryMap queryMap = new QueryMap(request.getParameterMap());
		logger.debug("queryMap tostring>> {}", queryMap);
		Integer timeout = request.getIntParameter("timeout", DEFAULT_TIMEOUT);
		String responseCharset = request.getParameter("responseCharset", DEFAULT_CHARSET);
		writeHeader(response, responseCharset);

		logger.debug("queryMap = {}", queryMap);
		logger.debug("timeout = {} s", timeout);
		if (timeout == null) {
			timeout = DEFAULT_TIMEOUT;
		}
		Writer writer = response.getWriter();
		response.setStatus(HttpResponseStatus.OK);
		Object obj = null;
		try {
			obj = doSearch(requestId, queryMap, timeout, writer);
		} finally {
//			writer.close();
			requestLogger.info("end request id:{}",requestId);
		}

		if(obj instanceof Exception) {
			Exception e = (Exception) obj;
			searchErrorLogger.error("REQ-{} URL: {}?{}", requestId, request.uri(), request.getParameterString());
            if(e instanceof SearchError) {
                searchErrorLogger.error("RES-{} : {}", requestId, e.getMessage());
            } else {
                searchErrorLogger.error("RES-" + requestId, e);
            }
		}

	}

	

	protected ResponseWriter getSearchResultWriter(Writer writer, boolean isFieldLowercase, boolean noUnicode) {
		return getSearchResultWriter(writer, "_search_callback", isFieldLowercase, noUnicode);
	}

	protected ResponseWriter getSearchResultWriter(Writer writer, String jsonCallback, boolean isFieldLowercase, boolean noUnicode) {
		return getResponseWriter(writer, ServiceAction.DEFAULT_ROOT_ELEMENT, true, jsonCallback, isFieldLowercase, noUnicode);
	}
}
