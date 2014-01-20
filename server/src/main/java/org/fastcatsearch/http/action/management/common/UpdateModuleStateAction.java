package org.fastcatsearch.http.action.management.common;

import java.io.Writer;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/common/update-modules-state", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class UpdateModuleStateAction extends AuthAction {
	
	@SuppressWarnings("unchecked")
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		boolean isSuccess = true;
		
		try {
			String action = request.getParameter("action");
			ServiceManager serviceManager = ServiceManager.getInstance();
			AbstractService service = null;
			
			String[] classNames = request.getParameter("services","").split(",");
			for(String className : classNames) {
				@SuppressWarnings("rawtypes")
				Class cls = Class.forName(className.trim());
				service = serviceManager.getService(cls);
				
				if("stop".equals(action) || "restart".equals(action)) {
					service.stop();
				}
				
				
				if("start".equals(action) || "restart".equals(action)) {
					service.start();
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			isSuccess = false;
		}
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		responseWriter.endObject();
		responseWriter.done();
	}
}
