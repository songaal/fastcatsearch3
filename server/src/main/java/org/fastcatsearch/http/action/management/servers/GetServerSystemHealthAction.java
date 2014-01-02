package org.fastcatsearch.http.action.management.servers;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.management.GetServerSystemHealthJob;
import org.fastcatsearch.job.management.GetServerSystemHealthJob.SystemHealthInfo;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;
/**
 * 데이터 : Node, JVM Path, JVM Version, JVM Option, Install Path
 * nodeId가 존재하면 하나의 데이터만 리턴하고 없으면 모든 노드의 데이터리턴.
 * 
 * */
@ActionMapping(value = "/management/servers/systemHealth", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.READABLE)
public class GetServerSystemHealthAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String nodeId = request.getParameter("nodeId");
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		
		List<Node> nodeList = nodeService.getNodeArrayList();
		if(nodeId != null && nodeId.length() > 0){
			List<Node> list = new ArrayList<Node>();
			for(Node node : nodeList){
				if(node.id().equals(nodeId)){
					list.add(node);
				}
			}
			nodeList = list;
		}
		
		responseWriter.object();
		GetServerSystemHealthJob job = new GetServerSystemHealthJob();
		NodeJobResult[] nodeJobResult = ClusterUtils.sendJobToNodeList(job, nodeService, nodeList, true);
		for(NodeJobResult jobResult : nodeJobResult) {
			if(jobResult.isSuccess()){
				Node node = jobResult.node();
				Object result = jobResult.result();
				if(result != null){
					SystemHealthInfo info = (SystemHealthInfo) result;
					writeSystemHealthInfo(info, node, responseWriter);
				}
				
			}
		}
		responseWriter.endObject();
		responseWriter.done();
	}

	private void writeSystemHealthInfo(SystemHealthInfo info, Node node, ResponseWriter responseWriter) throws ResultWriterException{
		responseWriter.key(node.id()).object()
		.key("nodeName").value(node.name())
		.key("jvmCpuUse").value(info.jvmCpuUse)
		.key("systemCpuUse").value(info.systemCpuUse)
		.key("systemLoadAverage").value(info.systemLoadAverage)
		.key("maxMemory").value(info.maxMemory)
		.key("committedMemory").value(info.committedMemory)
		.key("totalMemory").value(info.totalMemory)
		.key("usedMemory").value(info.usedMemory)
		.key("totalDiskSize").value(info.totalDiskSize)
		.key("usedDiskSize").value(info.usedDiskSize)
		.key("freeDiskSize").value(info.freeDiskSize)
		.endObject();
	}
}
