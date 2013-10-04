package org.fastcatsearch.http.action.management.servers;

import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping("/management/servers/list")
public class GetServerInfoListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		List<Node> nodeList = nodeService.getNodeList();
		
		responseWriter.object().key("nodeList").array("node");
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			responseWriter.object()
			.key("id").value(node.id())
			.key("name").value(node.name())
			.key("host").value(node.address().getHostName())
			.key("port").value(node.address().getPort())
			.key("enabled").value(node.isEnabled())
			.key("active").value(node.isActive())
			.endObject();
		}
		responseWriter.endArray().endObject();
		responseWriter.done();
	}

}
