package org.fastcatsearch.http.service.action;

import java.io.PrintWriter;

import org.fastcatsearch.http.ActionRequest;
import org.fastcatsearch.util.ResultWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class IsAliveAction extends ServiceAction {

	public IsAliveAction(String type){
		super(type);	
	}
	
	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		PrintWriter writer = response.getWriter();
		writeHeader(response);
		response.setStatus(HttpResponseStatus.OK);
		ResultWriter resultWriter = getResultWriter(writer, "fastcatsearch", true, null);
		resultWriter.object().key("status").value("ok").endObject();
		writer.close();
	}

}
