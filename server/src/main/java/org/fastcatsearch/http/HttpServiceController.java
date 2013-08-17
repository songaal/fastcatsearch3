package org.fastcatsearch.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fastcatsearch.service.action.ActionResponse;
import org.fastcatsearch.service.action.HttpAction;
import org.fastcatsearch.util.DynamicClassLoader;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServiceController {
	private static final Logger logger = LoggerFactory.getLogger(HttpServiceController.class);

	private ExecutorService executorService;
	Map<String, Class<HttpAction>> actionMap;
	
	public HttpServiceController(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public void dispatchRequest(HttpRequest request, HttpChannel httpChannel) {
		// request에서 uri를 보고 외부요청을 확인하여 channel에 최종 결과를 write한다.

		String uri = getAction(request);
		if (uri != null) {
			Class clazz = actionMap.get(uri);
			HttpAction action;
			try {
				action = (HttpAction) clazz.newInstance();
				action.setRequest(request, httpChannel);
				executorService.execute(action);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			ActionResponse response = new ActionResponse();
//			response.setContentType("json");
//			String contentStr = "<result><title>맛있는 미식가</title><price>1200</price></result>";
			
			response.setContentType("html");
			response.setStatus(HttpResponseStatus.OK);
			
			String contentStr = "<html><body><h1>제목</h1><h3>sub title</h3></body></html>";
			try {
				response.getWriter().write(contentStr);
			} catch (IOException e) {
				e.printStackTrace();
			}
			httpChannel.sendResponse(response);
		}
	}

	private String getAction(HttpRequest request) {
		String uri = request.getUri();
		logger.debug("URI : {}, method={}, version={}", uri, request.getMethod(), request.getProtocolVersion());
		logger.debug("headers : {}", request.getHeaderNames());
		long len = HttpHeaders.getContentLength(request);
		ChannelBuffer buffer =  request.getContent();
		String str = new String(buffer.array(), 0, (int) len);
		logger.debug(">> {} : content : {}",len, str);

		//
		// TODO
		//
		
		
		
		return null;
	}

	public void setActionMap(Map<String, Class<HttpAction>> actionMap) {
		this.actionMap = actionMap;
	}
}
