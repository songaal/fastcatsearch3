package org.fastcatsearch.http;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fastcatsearch.http.service.action.HttpAction;
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
		HttpAction action = createAction(request, httpChannel);
		if (action != null) {
			try {
				executorService.execute(action);
			} catch (Exception e) {
				logger.error("Action job allocation error!", e);
			}
		}else{
			httpChannel.sendError(HttpResponseStatus.NOT_FOUND, null);
		}
	}

	private HttpAction createAction(HttpRequest request, HttpChannel httpChannel) {
		String uri = request.getUri();
		logger.debug("URI : {}, method={}, version={}", uri, request.getMethod(), request.getProtocolVersion());
		// uri의 파라미터 제거
		int pos = uri.indexOf("?");
		if (pos > 0) {
			uri = uri.substring(0, pos);
		}
		Class<HttpAction> clazz = actionMap.get(uri);
		ActionRequest actionRequest = null;
		if(clazz == null) {
			return null;
		}else{
			actionRequest = new ActionRequest(uri, request);
		}
		
		HttpAction action = null;
		try {
			action = clazz.newInstance();
		} catch (Exception e) {
			logger.error("Action class instance error!", e);
			return null;
		}
		action.setRequest(actionRequest, httpChannel);
		return action;
	}

	public void setActionMap(Map<String, Class<HttpAction>> actionMap) {
		this.actionMap = actionMap;
	}
}
