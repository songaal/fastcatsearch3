package org.fastcatsearch.http.action;

import java.io.Writer;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.util.ResponseWriter;

public abstract class AuthAction extends ServiceAction {

	public final static String AUTH_KEY = "__auth";
	
	private ActionAuthority authority;
	private ActionAuthorityLevel authorityLevel;
	
	abstract public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception;

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		writeHeader(response);
		
		if(session == null){
			return;
		}
		Object obj = session.getAttribute(AUTH_KEY);
		
		try {
			
			//FIXME 일단 테스트시에는 auth없이..
			doAuthAction(request, response);
			/*
			if (obj == null) {
				// 인증 안되어 있음.
				doNotAuthenticatedResult(request, response);
			} else {
				doAuthAction(request, response);
			}
			*/
		} finally {
			response.getWriter().close();
		}
	}

	private void doNotAuthenticatedResult(ActionRequest request, ActionResponse response) throws Exception {
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter.object().key("error").value("Not Authenticated.").endObject();
		resultWriter.done();
	}

	public void setAuthority(ActionAuthority authority, ActionAuthorityLevel authorityLevel) {
		this.authority = authority;
		this.authorityLevel = authorityLevel;
	}
}
