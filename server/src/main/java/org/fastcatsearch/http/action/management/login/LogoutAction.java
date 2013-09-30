package org.fastcatsearch.http.action.management.login;

import java.io.Writer;

import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.http.action.ServiceAction;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/logout")
public class LogoutAction extends ServiceAction {

	@Override
	public void doAction(ActionRequest request, ActionResponse response) throws Exception {
		writeHeader(response);
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		resultWriter
		.object()
		.key("status").value("0")
		.key("id").value("oddeye")
		.endObject();
		resultWriter.done();
		writer.close();
		session.removeAttribute(AuthAction.AUTH_KEY);
	}

}
