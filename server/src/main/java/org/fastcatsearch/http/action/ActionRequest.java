package org.fastcatsearch.http.action;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

	private static final String DEFAULT_CHARSET = "utf-8";
	private HttpRequest request;
	private String uri;
	private String queryString;
	private Map<String, String> parameterMap;

	public ActionRequest(String uri, HttpRequest request) {
		this.uri = uri;
		this.request = request;
		
		if(request!=null) {
			if (request.getMethod() == HttpMethod.GET) {
//				logger.debug("URI:{} , {}, {}", request.getUri(), request.getUri().length(), uri.length());
				if (request.getUri().length() > uri.length()) {
					queryString = request.getUri().substring(uri.length() + 1); // 맨앞의 ?를 제거하기 위해 +1
				}
			} else if (request.getMethod() == HttpMethod.POST) {
				long len = HttpHeaders.getContentLength(request);
				ChannelBuffer buffer = request.getContent();
				queryString = new String(buffer.toByteBuffer().array(), 0, (int) len);
			} else {
	
			}
//			logger.debug("action {}, {}", uri, queryString);
//			try {
//				if(queryString != null){
//					queryString = URLDecoder.decode(queryString, DEFAULT_CHARSET);
////					logger.debug(">> {}", queryString);
//				}
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
			parameterMap = new HashMap<String, String>();
			if (queryString != null) {
				parse();
			}
		}
	}

	public boolean isMethodGet(){
		return request.getMethod() == HttpMethod.GET;
	}
	public boolean isMethodPost(){
		return request.getMethod() == HttpMethod.POST;
	}
	
	public HttpRequest request() {
		return request;
	}

	public String uri() {
		return uri;
	}

	public Map<String, String> getParameterMap() {
		return parameterMap;
	}
	
	public String getParameter(String key) {
		return getParameter(key, null);
	}

	public String getParameter(String key, String defaultValue) {
		if(parameterMap.containsKey(key)) {
			return parameterMap.get(key);
		} else {
			return defaultValue;
		}
	}

	public int getIntParameter(String key) {
		return getIntParameter(key, 0);
	}
	
	public int getIntParameter(String key, int defaultValue) {
		String value = getParameter(key);
		if (value != null) {
			try{
				return Integer.parseInt(value);
			}catch(NumberFormatException e){
			}
		}
		return defaultValue;
	}
	
	public boolean getBooleanParameter(String key) {
		return getBooleanParameter(key, false);
	}
	
	public boolean getBooleanParameter(String key, boolean defaultValue) {
		String value = getParameter(key);
		if (value != null) {
			return "true".equalsIgnoreCase(value);
		} else {
			return defaultValue;
		}
	}

	public String getParameterString() {
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
		parse(DEFAULT_CHARSET);
	}

	private void parse(String charset) {
		try {
			for (String pair : queryString.split("&")) {
				int eq = pair.indexOf("=");
				if (eq < 0) {
					// key with no value
//					parameterMap.put(pair.toUpperCase(), "");
					parameterMap.put(pair, "");
				} else {
					// key=value
					String key = pair.substring(0, eq);
					String value = pair.substring(eq + 1);
					String decodedValue = URLDecoder.decode(value, charset);
//					logger.debug("DECODE {} > {}", value, decodedValue);
					parameterMap.put(key, decodedValue);

				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("encoding error!", e);
		}
		
//		logger.debug("parameterMap >> {}", parameterMap);
	}

	
}
