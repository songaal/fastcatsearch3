package org.fastcatsearch.http;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionRequest {
	private static final Logger logger = LoggerFactory.getLogger(ActionRequest.class);

	private HttpRequest request;
	private String uri;
	private String queryString;
	private Map<String, String> parameterMap;

	public ActionRequest(String uri, HttpRequest request) {
		this.uri = uri;
		this.request = request;

		if (request.getMethod() == HttpMethod.GET) {
			logger.debug("URI:{} , {}, {}", request.getUri(), request.getUri().length(), uri.length());
			if (request.getUri().length() > uri.length()) {
				queryString = request.getUri().substring(uri.length() + 1);
			}
		} else if (request.getMethod() == HttpMethod.POST) {
			long len = HttpHeaders.getContentLength(request);
			ChannelBuffer buffer = request.getContent();
			queryString = new String(buffer.array(), 0, (int) len);
		} else {

		}
		logger.debug("action {}  ? {}", uri, queryString);
	}

	public HttpRequest request() {
		return request;
	}

	public String uri() {
		return uri;
	}

	public Map<String, String> parameterMap() {
		prepareParameterMap();
		return parameterMap;
	}

	public String getParameter(String key) {
		prepareParameterMap();
		return parameterMap.get(key);
	}

	public int getIntParameter(String key) {
		return Integer.parseInt(getParameter(key));
	}
	public String getQueryString() {
		return queryString;
	}

	private void prepareParameterMap() {
		if (parameterMap == null) {
			parameterMap = new HashMap<String, String>();
			if (queryString != null) {
				parse();
			}
		}
	}
	
	private void parse() {
		parse("utf-8");
	}
	private void parse(String charset) {
	    for (String pair : queryString.split("&")) {
	        int eq = pair.indexOf("=");
	        if (eq < 0) {
	            // key with no value
	            parameterMap.put(pair, "");
	        } else {
	            // key=value
//	            String key = URLDecoder.decode(pair.substring(0, eq), charset);
//	            String value = URLDecoder.decode(pair.substring(eq + 1), charset);
	        	String key = pair.substring(0, eq);
	        	String value = pair.substring(eq + 1);
	            parameterMap.put(key, value);
	        }
	    }
	}
}
