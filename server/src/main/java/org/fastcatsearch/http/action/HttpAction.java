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
	private ActionResponse response;
	protected Environment environment;
	protected HttpSession session;
	protected Type resultType;
	
	public HttpAction(){
	}
	
	public HttpAction clone(){
		HttpAction action = null;
		try {
			action = (HttpAction) super.clone();
			action.request = null;
			action.response = null;
			return action;
		} catch (CloneNotSupportedException e) {
			logger.error("Clone error", e);
		}
		return null;
	}
	
	public void init(Type resultType, ActionRequest request, ActionResponse response, HttpSession session){
		this.resultType = resultType;
		this.request = request;
		this.response = response;
		this.session = session;
		
	}
	
	abstract public void runAction(ActionRequest request, ActionResponse response) throws Exception;
		
	@Override
	public void run() {
		
		try {
			runAction(request, response);
			response.done();
        } catch (ActionException e) {
            response.error(e);
		} catch (Throwable e) {
			logger.error("Action수행중 에러발생.", e);
			response.error(e);
//			response.getChannel().sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, e);
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
