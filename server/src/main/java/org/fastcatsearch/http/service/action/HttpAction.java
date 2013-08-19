package org.fastcatsearch.http.service.action;

import org.fastcatsearch.http.ActionRequest;
import org.fastcatsearch.http.HttpChannel;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HttpAction implements Runnable {
	protected static final Logger logger = LoggerFactory.getLogger(HttpAction.class);
	
	private ActionRequest request;
	private HttpChannel httpChannel;
	private ActionResponse response;
	
	public HttpAction(){
	}
	
	public void setRequest(ActionRequest request, HttpChannel httpChannel){
		this.request = request;
		this.httpChannel = httpChannel;
		
	}
	
	abstract public void doAction(ActionRequest request, ActionResponse response) throws Exception;
		
	@Override
	public void run() {
		
		response = new ActionResponse();
		
		try {
			doAction(request, response);
			httpChannel.sendResponse(response);
		} catch (Throwable e) {
			logger.error("Action수행중 에러발생.", e);
			httpChannel.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, e);
		}
		
	}
	
}
