package org.fastcatsearch.http.action.service;

import java.io.Writer;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

@ActionMapping("/test")
public class TestAction extends ServiceAction {

	public String name;
	
	public TestAction() {
		name = "test";
	}

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		
		writeHeader(response);
		
		logger.info("parameter Map >> {}", request.getParameterMap());
		
		String contentStr = "<html><body><h1>제목</h1><h3>sub title</h3></body></html>";
		Writer writer = response.getWriter();
		try {
			writer.write(contentStr);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			if(writer != null){
				writer.close();
			}
		}
	
	}

}
