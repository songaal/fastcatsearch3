package org.fastcatsearch.http.action.setting;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.HttpRequestService;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping (value="/setting/authority/list")
public class AuthorityListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response)
			throws Exception {
		
		HttpRequestService service = ServiceManager.getInstance().getService(HttpRequestService.class);
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		
		ActionAuthority[] authorities = ActionAuthority.values();
		
		responseWriter.object();
		
		for(ActionAuthority authority : authorities) {
			
			if(authority == ActionAuthority.NULL) {
				continue;
			}
			
			responseWriter.key(authority.name()).value(authority.getName());
		}
		
		responseWriter.endObject();
		responseWriter.done();
	}
}
