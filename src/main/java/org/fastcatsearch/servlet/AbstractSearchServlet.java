package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.job.Job;
import org.fastcatsearch.util.JSONPResultStringer;
import org.fastcatsearch.util.JSONResultStringer;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.XMLResultStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSearchServlet extends JobHttpServlet {

	private static final long serialVersionUID = 4165287616751500253L;
	protected static AtomicLong taskSeq = new AtomicLong();
	protected static Logger searchLogger = LoggerFactory.getLogger("SEARCH_LOG");

	public static final int DEFAULT_TIMEOUT = 5; //5초.
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

		logger.debug("queryString = " + queryString);
		logger.debug("timeout = " + timeout + " s");

		long seq = taskSeq.incrementAndGet();
		searchLogger.info("{},{}", seq, queryString);

		doSearch(queryString, response);

	}

	protected abstract void doSearch(String queryString, HttpServletResponse response) throws ServletException, IOException;

	protected abstract Job createSearchJob(String queryString);

	protected abstract AbstractSearchResultWriter createSearchResultWriter(Writer writer);

	public void writeHeader(HttpServletResponse response, ResultStringer stringer) {
		response.reset();
		if (stringer instanceof JSONResultStringer) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (stringer instanceof JSONPResultStringer) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (stringer instanceof XMLResultStringer) {
			response.setContentType("text/xml; charset=" + responseCharset);
		}
	}

	public void prepare(HttpServletRequest request) {
		requestCharset = getParameter(request, "requestCharset", "UTF-8");
		responseCharset = getParameter(request, "responseCharset", "UTF-8");
		String timeoutStr = getParameter(request, "timeout", Integer.toString(DEFAULT_TIMEOUT));
		try {
			timeout = Integer.parseInt(timeoutStr);
		} catch (NumberFormatException e) {
			timeout = DEFAULT_TIMEOUT;
		}
		isAdmin = "true".equals(request.getParameter("admin"));

		collectionName = getParameter(request, "cn", "");
		fields = getParameter(request, "fl", "");
		searchCondition = getParameter(request, "se", "");
		groupFields = getParameter(request, "gr", "");
		groupCondition = getParameter(request, "gc", "");
		groupFilter = getParameter(request, "gf", "");
		sortFields = getParameter(request, "ra", "");
		filterFields = getParameter(request, "ft", "");
		startNumber = getParameter(request, "sn", "");
		resultLength = getParameter(request, "ln", "");
		highlightTags = getParameter(request, "ht", "");
		searchOption = getParameter(request, "so", "");
		userData = getParameter(request, "ud", "");
		jsonCallback = getParameter(request, "jsoncallback", "");
	}

	protected String getParameter(HttpServletRequest request, String key, String defaultValue) {
		String value = request.getParameter(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public String queryString() {
		try {
			return "cn=" + collectionName + "&fl=" + URLDecoder.decode(fields, requestCharset) + "&se="
					+ URLDecoder.decode(searchCondition, requestCharset) + "&gr="
					+ URLDecoder.decode(groupFields, requestCharset) + "&gc=" + URLDecoder.decode(groupCondition, requestCharset)
					+ "&gf=" + URLDecoder.decode(groupFilter, requestCharset) + "&ra="
					+ URLDecoder.decode(sortFields, requestCharset) + "&ft=" + URLDecoder.decode(filterFields, requestCharset)
					+ "&sn=" + URLDecoder.decode(startNumber, requestCharset) + "&ln="
					+ URLDecoder.decode(resultLength, requestCharset) + "&ht=" + URLDecoder.decode(highlightTags, requestCharset)
					+ "&so=" + URLDecoder.decode(searchOption, requestCharset) + "&ud="
					+ URLDecoder.decode(userData, requestCharset);
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
		}
		return "";
	}

	public ResultStringer getResultStringer() {
		ResultStringer rStringer = null;
		if (resultType == JSON_TYPE) {
			rStringer = new JSONResultStringer();
		} else if (resultType == JSONP_TYPE) {
			rStringer = new JSONPResultStringer(jsonCallback);
		} else if (resultType == XML_TYPE) {
			rStringer = new XMLResultStringer("fastcat", true);
		}
		return rStringer;
	}
}
