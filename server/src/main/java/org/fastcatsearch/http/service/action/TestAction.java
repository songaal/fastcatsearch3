package org.fastcatsearch.http.service.action;

import org.fastcatsearch.http.ActionRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class TestAction extends ServiceAction {

	public String name;
	
	public TestAction(String type) {
		super(type);
		name = "test";
	}

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		
		writeHeader(response);
		
		String contentStr = "<html><body><h1>제목</h1><h3>sub title</h3></body></html>";
		try {
			response.getWriter().write(contentStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		response.setStatus(HttpResponseStatus.OK);
		
		
	}

}
