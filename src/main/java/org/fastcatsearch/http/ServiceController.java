package org.fastcatsearch.http;

import java.util.concurrent.ExecutorService;

import org.fastcatsearch.common.DynamicClassLoader;
import org.fastcatsearch.service.action.HttpAction;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class ServiceController {
	ExecutorService executorService;
	
	public ServiceController(ExecutorService executorService) {
		this.executorService  = executorService;
	}

	public void dispatchRequest(HttpRequest request, HttpChannel httpChannel) {
		//request에서 uri를 보고 외부요청을 확인하여 channel에 최종 결과를 write한다.  
		
		String actionClassName = getAction(request);
		HttpAction action = DynamicClassLoader.loadObject(actionClassName, HttpAction.class, new Class<?>[]{HttpRequest.class, HttpChannel.class}, new Object[]{request, httpChannel});
		executorService.execute(action);
	}

	private String getAction(HttpRequest request) {
		String uri = request.getUri();
		
		//
		//TODO
		//
		return null;
	}


}
