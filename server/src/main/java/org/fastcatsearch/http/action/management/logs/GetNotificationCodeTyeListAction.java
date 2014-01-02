package org.fastcatsearch.http.action.management.logs;

import java.io.Writer;
import java.util.Map.Entry;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.notification.message.Notification;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value="/management/logs/notification-code-type-list", authority=ActionAuthority.Logs)
public class GetNotificationCodeTyeListAction extends AuthAction {
	
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
			
			
			resultWriter.object()
				.key("code-type-list").array();
			
			for(Entry<String, String> entry : Notification.getNotificationCodeDefinition().entrySet()){
				
				String code = entry.getKey();
				String codeType = entry.getValue();
				
				resultWriter.object()
					.key("code").value(code)
					.key("codeType").value(codeType)
					.endObject();
				
			}
			resultWriter.endArray().endObject();
		resultWriter.done();
	}
}
