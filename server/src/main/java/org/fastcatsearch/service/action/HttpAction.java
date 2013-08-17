package org.fastcatsearch.service.action;

import org.fastcatsearch.http.HttpChannel;
import org.jboss.netty.handler.codec.http.HttpRequest;

public abstract class HttpAction implements Runnable {
	
	HttpRequest request;
	HttpChannel httpChannel;
	ActionResponse response;
	
	public HttpAction(){
	}
	
	public void setRequest(HttpRequest request, HttpChannel httpChannel){
		this.request = request;
		this.httpChannel = httpChannel;
	}
	
	abstract public void doAction(HttpRequest request, ActionResponse response) throws ActionException;
		
	@Override
	public void run() {
		
		ActionResponse response = null;
		
		try {
			doAction(request, response);
			httpChannel.sendResponse(response);
		} catch (ActionException e) {
			e.printStackTrace();
			
			//httpChannel 에 에러전달.
			
		} finally {
			
		}
		
		
	}
	
}
