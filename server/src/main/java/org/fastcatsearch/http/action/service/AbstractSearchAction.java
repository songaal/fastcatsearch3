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
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.query.QueryMap;
import org.fastcatsearch.query.QueryParser;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSearchAction extends ServiceAction {

	private static AtomicLong taskSeq = new AtomicLong();
	protected static Logger searchLogger = LoggerFactory.getLogger("SEARCH_LOG");

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
		String collectionId = extractSearchCollectionId(queryMap);
		String searchKeyword = extractSearchKeyowrd(queryMap);
		writeSearchLog(requestId, collectionId, searchKeyword, obj, searchTime);

		AbstractSearchResultWriter resultWriter = createSearchResultWriter(writer);

		try {
			resultWriter.writeResult(obj, searchTime, jobResult.isSuccess());
		} catch (ResultWriterException e) {
			logger.error("", e);
		}

		writer.close();

	}

	private String extractSearchCollectionId(QueryMap queryMap) {
		return queryMap.get(Query.EL.cn.name());
	}

	private String extractSearchKeyowrd(QueryMap queryMap) {
		String udString = queryMap.get(Query.EL.ud.name()); //FIXME ud가 대문자로 들어올경우는 문제될수 있다.
		if (udString != null) {
			String els[] = udString.split(",");
			for (String el : els) {
				String[] kv = el.split(":");
				if (kv.length == 2) {
					if (kv[0].equalsIgnoreCase("keyword")) {
						return kv[1];
					}
				}
			}
		}
		return "";
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

	private static String LOG_DELIMITER = "\t";
	protected void writeSearchLog(long requestId, String collectionId, String searchKeyword, Object obj, long searchTime) {
		if (obj instanceof Result) {
			Result result = (Result) obj;
			StringBuffer logBuffer = new StringBuffer();
			logBuffer.append(requestId);
			logBuffer.append(LOG_DELIMITER);
			
			logBuffer.append(collectionId);
			logBuffer.append(LOG_DELIMITER);
			
			logBuffer.append(searchKeyword);
			logBuffer.append(LOG_DELIMITER);
			
			logBuffer.append(searchTime);
			logBuffer.append(LOG_DELIMITER);
			
			logBuffer.append(result.getCount());
			logBuffer.append(LOG_DELIMITER);
			
			logBuffer.append(result.getTotalCount());
			
			GroupResults groupResults = result.getGroupResult();
			if(groupResults != null){
				logBuffer.append(LOG_DELIMITER);
				int groupSize = groupResults.groupSize();
				for (int i = 0; i < groupSize; i++) {
					GroupResult groupResult = groupResults.getGroupResult(i);
					if (i > 0) {
						logBuffer.append(";");
					}
					logBuffer.append(groupResult.size());
				}
			}
			searchLogger.info(logBuffer.toString());
		}
	}

	protected ResponseWriter getSearchResultWriter(Writer writer) {
		return getSearchResultWriter(writer, "_search_callback");
	}

	protected ResponseWriter getSearchResultWriter(Writer writer, String jsonCallback) {
		return getResponseWriter(writer, ServiceAction.DEFAULT_ROOT_ELEMENT, true, jsonCallback);
	}
}
