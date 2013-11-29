package org.fastcatsearch.http.action;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.http.ActionMethod;
import org.fastcatsearch.http.HttpChannel;
import org.fastcatsearch.http.HttpSession;
import org.fastcatsearch.http.action.ServiceAction.Type;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HttpAction implements Runnable, Cloneable {
	protected static final Logger logger = LoggerFactory.getLogger(HttpAction.class);
	private ActionMethod[] method; //허용 http 메소드.
	
	private ActionRequest request;
	private HttpChannel httpChannel;
	private ActionResponse response;
	protected Environment environment;
	protected HttpSession session;
	protected Type resultType;
	
	public HttpAction(){
	}
	
	public HttpAction clone(){
		HttpAction action;
		try {
			action = (HttpAction) super.clone();
			action.request = null;
			action.httpChannel = null;
			action.response = null;
			return action;
		} catch (CloneNotSupportedException e) {
			logger.error("Clone error", e);
		}
		return null;
	}
	
	public void init(Type resultType, ActionRequest request, ActionResponse response, HttpSession session, HttpChannel httpChannel){
		this.resultType = resultType;
		this.request = request;
		this.response = response;
		this.session = session;
		this.httpChannel = httpChannel;
		response.init();
		
	}
	
	abstract public void doAction(ActionRequest request, ActionResponse response) throws Exception;
		
	@Override
	public void run() {
		
		try {
			doAction(request, response);
			httpChannel.sendResponse(response);
		} catch (Throwable e) {
			logger.error("Action수행중 에러발생.", e);
			httpChannel.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, e);
		}
		
	}

	public boolean isMethod(ActionMethod actionMethod){
		for(ActionMethod m : this.method){
			if(m == actionMethod){
				return true;
			}
		}
		return false;
	}
	
	public void setMethod(ActionMethod[] method) {
		this.method = method;
	}
	
	public void setEnvironement(Environment environment) {
		this.environment = environment;
	}
}
