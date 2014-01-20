package org.fastcatsearch.http.action.management.common;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/common/modules-running-state", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.NONE)
public class GetModuleStateAction extends AuthAction {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		try {
			
			ServiceManager serviceManager = ServiceManager.getInstance();
			
			AbstractService service = null;
			Set<String> serviceNames = new TreeSet<String>();
			Map<String,String> serviceNameMap = new HashMap<String, String>();
			Map<String,Boolean> serviceStateMap = new HashMap<String, Boolean>();
			for(Class serviceClass : serviceManager.getServiceClasses()) {
				String[] fqdn = serviceClass.getName().split("[.]");
				String serviceName = fqdn[fqdn.length-1];
				serviceNames.add(serviceName);
				service = serviceManager.getService(serviceClass);
				serviceNameMap.put(serviceName, serviceClass.getName());
				serviceStateMap.put(serviceName, service.isRunning());
			}
			
			Writer writer = response.getWriter();
			ResponseWriter resultWriter = getDefaultResponseWriter(writer);
			resultWriter.object().key("moduleState").array();
			
			for(String serviceName : serviceNames) {
				resultWriter.object()
					.key("serviceName").value(serviceName)
					.key("serviceClass").value(serviceNameMap.get(serviceName))
					.key("status").value(serviceStateMap.get(serviceName))
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
