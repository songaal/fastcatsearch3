package org.fastcatsearch.http.action.management.servers;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.management.RestartCatServerJob;
import org.fastcatsearch.job.management.ShutdownCatServerJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/servers/shutdown", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class ShutdownServerAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		String nodeId = request.getParameter("nodeId");
		
		if(nodeId == null || nodeId.trim().length() == 0){
			throw new IllegalArgumentException("nodeId is empty");
		}
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node node = nodeService.getNodeById(nodeId);
		
		if(node == null){
			throw new IllegalArgumentException("Unknown node is = " + nodeId);
		}
		
		nodeService.sendRequest(node, new ShutdownCatServerJob());
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		
		responseWriter.object()
			.key("status").value(0)
			.key("success").value(true)
		.endObject();
		responseWriter.done();
	}

}
