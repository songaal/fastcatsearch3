package org.fastcatsearch.http.action.management.collections;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.management.RestoreCollectionToPreviousJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

/**
 * 이전 시퀀스로 컬렉션을 복원한다.
 * 
 * */
@ActionMapping(value = "/management/collections/restore-to-previous", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class RestoreCollectionToPreviousAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		String nodeIdListString = request.getParameter("nodeList");
		List<String> destNodeIdList = new ArrayList<String>();
		if (nodeIdListString != null) {
			for (String nodeStr : nodeIdListString.split(",")) {
				nodeStr = nodeStr.trim();
				if (nodeStr.length() > 0) {
					destNodeIdList.add(nodeStr);
				}
			}
		}
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Job job = new RestoreCollectionToPreviousJob(collectionId, null);
		NodeJobResult[] jobResultList = ClusterUtils.sendJobToNodeIdList(job, nodeService, destNodeIdList, true);
		int successCount = 0;
		for(NodeJobResult result : jobResultList){
			if(result.isSuccess()){
				Object obj = result.result();
				successCount++;
			}
		}
		ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
		responseWriter.object();
		responseWriter.key("success").value(true);
		responseWriter.key("result").value("");
		responseWriter.endObject();
		responseWriter.done();
		
	}

}
