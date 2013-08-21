package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.util.ResultWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractSearchServlet extends WebServiceHttpServlet {

	private static final long serialVersionUID = 4165287616751500253L;
	protected static AtomicLong taskSeq = new AtomicLong();
	protected static Logger searchLogger = LoggerFactory.getLogger("SEARCH_LOG");

	public static final int DEFAULT_TIMEOUT = 5; // 5초.
	public static final int IS_ALIVE = 3;

	protected String requestCharset;
	protected String responseCharset;
	protected boolean isAdmin;
	protected int timeout;

	protected String collectionName;
	protected String fields;
	protected String searchCondition;
	protected String groupFields;
	protected String groupCondition;
	protected String groupFilter;
	protected String sortFields;
	protected String filterFields;
	protected String startNumber;
	protected String resultLength;
	protected String highlightTags;
	protected String searchOption;
	protected String userData;
	protected String jsonCallback;

	public AbstractSearchServlet(int resultType) {
		super(resultType);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doReal(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doReal(request, response);
	}

	private void doReal(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		prepare(request);

		if (resultType == IS_ALIVE) {
			response.setContentType("text/html; charset=" + responseCharset);
			response.getWriter().write("FastCat/OK\n<br/>" + new Date());
			return;
		}

		String queryString = queryString();

		logger.debug("queryString = {}", queryString);
		logger.debug("timeout = {} s", timeout);

		long requestId = taskSeq.incrementAndGet();
		searchLogger.info("{},{}", requestId, queryString);

		doSearch(requestId, queryString, response);

	}

	protected abstract void doSearch(long requestId, String queryString, HttpServletResponse response) throws ServletException, IOException;

	protected abstract Job createSearchJob(String queryString);

	protected abstract AbstractSearchResultWriter createSearchResultWriter(Writer writer);

	public void prepare(HttpServletRequest request) {
		
		Map<String,String[]> parameters = request.getParameterMap();
		
		requestCharset = getParameterFromMap(parameters, "requestCharset", "UTF-8");
		responseCharset = getParameterFromMap(parameters, "responseCharset", "UTF-8");
		try {
			timeout = Integer.parseInt(getParameterFromMap(parameters, "timeout", String.valueOf(DEFAULT_TIMEOUT)));
		} catch (NumberFormatException e) {
			timeout = DEFAULT_TIMEOUT;
		}
		isAdmin = "true".equals(getParameterFromMap(parameters, "admin", ""));
		collectionName = getParameterFromMap(parameters, "cn", "");
		fields = getParameterFromMap(parameters, "fl", "");
		searchCondition = getParameterFromMap(parameters, "se", "");
		groupFields = getParameterFromMap(parameters, "gr", "");
		groupCondition = getParameterFromMap(parameters, "gc", "");
		groupFilter = getParameterFromMap(parameters, "gf", "");
		sortFields = getParameterFromMap(parameters, "ra", "");
		filterFields = getParameterFromMap(parameters, "ft", "");
		startNumber = getParameterFromMap(parameters, "sn", "");
		resultLength = getParameterFromMap(parameters, "ln", "");
		highlightTags = getParameterFromMap(parameters, "ht", "");
		searchOption = getParameterFromMap(parameters, "so", "");
		userData = getParameterFromMap(parameters, "ud", "");
		jsonCallback = getParameterFromMap(parameters, "jsoncallback", "");
	}

	public String queryString() {
		try {
			return "cn=" + collectionName + "&fl=" + URLDecoder.decode(fields, requestCharset) + "&se=" + URLDecoder.decode(searchCondition, requestCharset) + "&gr="
			                + URLDecoder.decode(groupFields, requestCharset) + "&gc=" + URLDecoder.decode(groupCondition, requestCharset) + "&gf="
			                + URLDecoder.decode(groupFilter, requestCharset) + "&ra=" + URLDecoder.decode(sortFields, requestCharset) + "&ft="
			                + URLDecoder.decode(filterFields, requestCharset) + "&sn=" + URLDecoder.decode(startNumber, requestCharset) + "&ln="
			                + URLDecoder.decode(resultLength, requestCharset) + "&ht=" + URLDecoder.decode(highlightTags, requestCharset) + "&so="
			                + URLDecoder.decode(searchOption, requestCharset) + "&ud=" + URLDecoder.decode(userData, requestCharset);
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
		}
		return "";
	}

	public void writeHeader(HttpServletResponse response, ResultWriter stringer) {
		writeHeader(response, stringer, responseCharset);
	}

	public ResultWriter getResultStringer() {
		return getResultStringer("fastcatsearch", isAdmin, jsonCallback);
	}

	protected void writeSearchLog(long requestId, Object obj, long searchTime) {
		if (obj instanceof Result) {
			Result result = (Result) obj;
			String logStr = requestId + "," + searchTime + "," + result.getCount() + "," + result.getTotalCount();
			searchLogger.info(logStr);
		}
	}
}
