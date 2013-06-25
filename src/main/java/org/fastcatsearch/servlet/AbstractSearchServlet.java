package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.util.ResultStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSearchServlet extends WebServiceHttpServlet {

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

		long requestId = taskSeq.incrementAndGet();
		searchLogger.info("{},{}", requestId, queryString);

		doSearch(requestId, queryString, response);

	}

	protected abstract void doSearch(long requestId, String queryString, HttpServletResponse response) throws ServletException, IOException;

	protected abstract Job createSearchJob(String queryString);

	protected abstract AbstractSearchResultWriter createSearchResultWriter(Writer writer);

	public void prepare(HttpServletRequest request) {
    	if(requestCharset == null) {
    		requestCharset = "UTF-8";
    	}
    	
    	if(responseCharset == null) {
    		responseCharset = "UTF-8";
    	}
    	
		requestCharset = getParameter(request, "requestCharset", "UTF-8");
		responseCharset = getParameter(request, "responseCharset", "UTF-8");
    	
    	String queryString = request.getQueryString();
    	
    	Map<String,String> kvmap = new HashMap<String,String>();
    	
    	try {
	    	if(queryString != null){
	    		queryString = URLDecoder.decode(queryString, requestCharset);
	    		if(queryString.endsWith("&")) { 
	    			queryString = queryString.substring(0,queryString.length()-1); 
	    		}
	    	}
	    	logger.debug("queryString = "+queryString);
    	} catch (UnsupportedEncodingException e) {
    	}
    	
		Pattern ptn1 = Pattern.compile("[?&]+([^&]*)");
		Pattern ptn2 = Pattern.compile("([^=]+)=(.*)");
    	
    	Matcher mat1 = ptn1.matcher(queryString);
    	Matcher mat2 = null;
    	
    	while(mat1.find()) {
    		mat2 = ptn2.matcher(mat1.group(1));
    		if(mat2.find()) {
    			kvmap.put(mat2.group(1), mat2.group(2));
    		}
    	}
		
		String timeoutStr = getParameter(kvmap, "timeout", Integer.toString(DEFAULT_TIMEOUT));
		try {
			timeout = Integer.parseInt(timeoutStr);
		} catch (NumberFormatException e) {
			timeout = DEFAULT_TIMEOUT;
		}
		isAdmin = "true".equals(request.getParameter("admin"));

		collectionName = getParameter(kvmap, "cn", "");
		fields = getParameter(kvmap, "fl", "");
		searchCondition = getParameter(kvmap, "se", "");
		groupFields = getParameter(kvmap, "gr", "");
		groupCondition = getParameter(kvmap, "gc", "");
		groupFilter = getParameter(kvmap, "gf", "");
		sortFields = getParameter(kvmap, "ra", "");
		filterFields = getParameter(kvmap, "ft", "");
		startNumber = getParameter(kvmap, "sn", "");
		resultLength = getParameter(kvmap, "ln", "");
		highlightTags = getParameter(kvmap, "ht", "");
		searchOption = getParameter(kvmap, "so", "");
		userData = getParameter(kvmap, "ud", "");
		jsonCallback = getParameter(kvmap, "jsoncallback", "");
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
	
	public void writeHeader(HttpServletResponse response, ResultStringer stringer) {
		writeHeader(response, stringer, responseCharset);
	}
	
	public ResultStringer getResultStringer() {
		return getResultStringer("fastcat",isAdmin, jsonCallback);
	}

	protected void writeSearchLog(long requestId, Object obj, long searchTime){
    	if(obj instanceof Result){
			Result result = (Result) obj;
			String logStr = requestId+","+searchTime+","+result.getCount()+","+result.getTotalCount();
			if(result.getGroupResult() != null){
				String grStr = ",";
				GroupResults groupResults = result.getGroupResult();
				GroupResult[] gr = groupResults.groupResultList();
				for (int i = 0; i < gr.length; i++) {
					if(i > 0)
						grStr += ",";
					grStr += gr[i].size();
				}
				logStr += grStr;
			}
			searchLogger.info(logStr);
			
		}else if(obj instanceof GroupResults){
			GroupResults groupResults = (GroupResults) obj;
			GroupResult[] gr = groupResults.groupResultList();
			String grStr = requestId+",0,0,0";
			for (int i = 0; i < gr.length; i++) {
				if(i > 0)
					grStr += ",";
				grStr += gr[i].size();
			}
			searchLogger.info(grStr);
		}
    }
}
