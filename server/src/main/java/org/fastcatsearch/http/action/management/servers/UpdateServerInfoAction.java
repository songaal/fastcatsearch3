package org.fastcatsearch.http.action.management.servers;

import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.env.SettingManager;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.cluster.NodeListUpdateJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.NodeListSettings;
import org.fastcatsearch.settings.NodeListSettings.NodeSettings;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/servers/update")
public class UpdateServerInfoAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		boolean isSuccess = false;
		
		try {
			
			String mode = request.getParameter("mode");
			
			int serverIndex = request.getIntParameter("serverIndex",0);
			String nodeId = request.getParameter("id");
			String name = request.getParameter("name");
			String address = request.getParameter("host");
			int port = request.getIntParameter("port",0);
			boolean enable = "true".equals(request.getParameter("enable"));
			
			SettingManager settingManager = environment.settingManager();
			NodeListSettings nodeListSettings = settingManager.getNodeListSettings();
			List<NodeSettings> nodeSettingList = nodeListSettings.getNodeList();
			
			NodeSettings settings =  new NodeSettings();
			settings.setId(nodeId);
			settings.setName(name);
			settings.setAddress(address);
			settings.setPort(port);
			settings.setEnabled(enable);
			
			if(serverIndex==-1) {
				nodeSettingList.add(settings);
			} else {
				nodeSettingList.set(serverIndex,settings);
			}
			
			if("delete".equals(mode)) {
				nodeSettingList.remove(serverIndex);
			}
			
			JobService jobService = JobService.getInstance();
			
			NodeListUpdateJob job = new NodeListUpdateJob();
			job.setArgs(nodeListSettings);
			jobService.offer(job).take();
			
			isSuccess = true;
			
		} catch (Exception e) {
			logger.error("",e);
			isSuccess = false;
		}
		
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		responseWriter.endObject();
		responseWriter.done();
	}
}
