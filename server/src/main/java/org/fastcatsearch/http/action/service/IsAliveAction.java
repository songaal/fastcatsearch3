package org.fastcatsearch.http.action.service;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.util.ResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

@ActionMapping("/service/isAlive")
public class IsAliveAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		writeHeader(response);
		response.setStatus(HttpResponseStatus.OK);
		ResponseWriter resultWriter = getDefaultResponseWriter(response.getWriter());
		resultWriter.object().key("status").value("ok").endObject();
		resultWriter.done();
	}

}
