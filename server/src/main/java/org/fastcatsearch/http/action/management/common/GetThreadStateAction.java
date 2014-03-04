package org.fastcatsearch.http.action.management.common;

import java.io.Writer;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.management.GetThreadStateJob;
import org.fastcatsearch.job.result.BasicStringResult;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.JSONWrappedResultWriter;
import org.fastcatsearch.util.ResponseWriter;
import org.json.JSONObject;

/**
 * 해당 노드의 Thread 리스트 상태를 리턴한다.  
 * */
@ActionMapping(value = "/management/common/thread-state", authority = ActionAuthority.Servers, authorityLevel = ActionAuthorityLevel.NONE)
public class GetThreadStateAction extends AuthAction {
	
	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String nodeId = request.getParameter("nodeId");
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node node = nodeService.getNodeById(nodeId);
		GetThreadStateJob job = new GetThreadStateJob();
		ResultFuture resultFuture = nodeService.sendRequest(node, job);
		
		if(resultFuture!=null) {
		
			Object obj = resultFuture.take();
			
			if(obj instanceof BasicStringResult) {
				
				BasicStringResult result = (BasicStringResult) obj;
				
				String resultStr = result.getResult();
				
				JSONObject root = new JSONObject(resultStr);
				
				JSONWrappedResultWriter wrapper = new JSONWrappedResultWriter(root, responseWriter);
				
				wrapper.wrap();
			}
		}
		responseWriter.done();
	}
}
