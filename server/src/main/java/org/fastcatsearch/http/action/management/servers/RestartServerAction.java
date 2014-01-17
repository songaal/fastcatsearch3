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
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/servers/restart", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class RestartServerAction extends AuthAction {

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
		
		nodeService.sendRequest(node, new RestartCatServerJob());
		
		Thread.sleep(3000);

		int count = 0;
		while(!node.isActive()){
			//1초씩 서버상태를 확인한다.
			Thread.sleep(1000);
			count++;
			if(count > 120){
				//2분이 되면 포기한다.
				break;
			}
		}
		
		boolean isSuccess = node.isActive();
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		
		responseWriter.object()
			.key("status").value(0)
			.key("success").value(isSuccess)
		.endObject();
		responseWriter.done();
	}

}
