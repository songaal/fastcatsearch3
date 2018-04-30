package org.fastcatsearch.http.action.management.servers;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

import java.util.List;

@ActionMapping(value = "/management/servers/list", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.NONE)
public class GetServerInfoListAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		
		String nodeId = request.getParameter("nodeId");
		
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		List<Node> nodeList = nodeService.getNodeArrayList();
		
		responseWriter.object().key("nodeList").array("node");
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			
			if(!(nodeId==null || nodeId.equals(node.id()))) {
				continue;
			}

			String dataAddress = new String();
			if (node.dataAddress() != null && node.dataAddress().getAddress() != null ){
				dataAddress = node.dataAddress().getAddress().getHostAddress();
			}

			responseWriter.object()
			.key("id").value(node.id())
			.key("name").value(node.name())
			.key("host").value(node.address().getAddress().getHostAddress())
			.key("dataHost").value(dataAddress)
			.key("port").value(node.port())
			.key("servicePort").value(node.servicePort())
			.key("enabled").value(node.isEnabled())
			.key("active").value(node.isActive())
			.endObject();
		}
		responseWriter.endArray().endObject();
		responseWriter.done();
	}

}
