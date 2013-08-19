package org.fastcatsearch.http.service.action;

import java.io.IOException;

import org.fastcatsearch.http.ActionRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class TestAction extends HttpAction {

	public TestAction() {
	}

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		
		response.setContentType("html");
		
		String contentStr = "<html><body><h1>제목</h1><h3>sub title</h3></body></html>";
		try {
			response.getWriter().write(contentStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		response.setStatus(HttpResponseStatus.OK);
		
		
	}

}
