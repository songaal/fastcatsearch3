package org.fastcatsearch.http.action.management.common;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.management.UpdateModuleStateJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/common/update-modules-state", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class UpdateModuleStateAction extends AuthAction {
	
	@SuppressWarnings("unchecked")
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		boolean isSuccess = false;
		String nodeId = request.getParameter("nodeId");
		String action = request.getParameter("action");
		String classNames = request.getParameter("services","");
		
		UpdateModuleStateJob job = new UpdateModuleStateJob();
		
		job.setAction(action);
		job.setServiceClasses(classNames);
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node node = nodeService.getNodeById(nodeId);
		ResultFuture resultFuture = nodeService.sendRequest(node, job);
		
		
		if(resultFuture!=null) {
		
			Object obj = resultFuture.take();
			
			if(Boolean.TRUE.equals(obj)) {
				isSuccess = true;
			}
		}
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(isSuccess);
		responseWriter.endObject();
		responseWriter.done();
	}
}
