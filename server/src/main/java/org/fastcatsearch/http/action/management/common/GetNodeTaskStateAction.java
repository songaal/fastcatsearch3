package org.fastcatsearch.http.action.management.common;

import java.io.Writer;
import java.util.List;
import java.util.Map.Entry;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.management.GetTaskStateJob;
import org.fastcatsearch.job.result.BasicStringResult;
import org.fastcatsearch.job.state.TaskKey;
import org.fastcatsearch.job.state.TaskState;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.JSONWrappedResultWriter;
import org.fastcatsearch.util.ResponseWriter;
import org.json.JSONObject;

@ActionMapping("/management/common/node-task-state")
public class GetNodeTaskStateAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String nodeId = request.getParameter("nodeId");
		
		Writer writer = response.getWriter();
		ResponseWriter responseWriter = getDefaultResponseWriter(writer);
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node node = nodeService.getNodeById(nodeId);
		
		GetTaskStateJob job = new GetTaskStateJob();
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
