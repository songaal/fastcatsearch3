package org.fastcatsearch.http.action;

import java.io.Writer;

import org.fastcatsearch.util.ResultWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public abstract class AuthAction extends ServiceAction {

	public final static String AUTH_KEY = "__auth";

	public AuthAction(String type) {
		super(type);
	}

	abstract public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception;

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		writeHeader(response);
		
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
		ResultWriter resultWriter = getDefaultResultWriter(writer);
		resultWriter.object().key("error").value("Not Authenticated.").endObject();
		resultWriter.done();
	}
}
