package org.fastcatsearch.http.action.management.common;

import java.io.Writer;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.HttpRequestService;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.management.SystemInfoService;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/common/modules-running-state", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.NONE)
public class GetModuleStateAction extends AuthAction {
	
	@SuppressWarnings("rawtypes")
	Class[] classes = new Class[] {
		IRService.class,
		NodeService.class,
		DBService.class,
		JobService.class,
		SystemInfoService.class,
		HttpRequestService.class,
		NotificationService.class
	};
	
	@SuppressWarnings("unchecked")
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		try {
			
			ServiceManager serviceManager = ServiceManager.getInstance();
			
			AbstractService service = null;
			
			Writer writer = response.getWriter();
			ResponseWriter resultWriter = getDefaultResponseWriter(writer);
			resultWriter.object().key("moduleState").array();
			for(Class<? extends AbstractService> cls : classes) {
				String[] fqdn = cls.getName().split("[.]");
				String className = fqdn[fqdn.length-1];
				service = serviceManager.getService(cls);
				resultWriter.object()
					.key("name").value(className)
					.key("status").value(service.isRunning())
					.endObject();
			}
			resultWriter .endArray()
			.endObject()
			.done();
		} catch (Exception e) {
			logger.error("", e);
		}
	}
}
