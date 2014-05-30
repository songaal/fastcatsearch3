package org.fastcatsearch.http.action.management.collections;

import java.util.HashSet;
import java.util.Set;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.job.management.CreateCollectionJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

/**
 * 관리도구 컬렉션 생성 위자드 step1 에서 사용될 액션.
 * */
@ActionMapping(value = "/management/collections/create-update", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class CreateCollectionAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		String collectionName = request.getParameter("name");
		String indexNode = request.getParameter("indexNode");
		String searchNodeListString = request.getParameter("searchNodeList");
		String dataNodeListString = request.getParameter("dataNodeList");

		boolean isSuccess = false;
		String errorMessage = "";

		try {

			CreateCollectionJob createCollectionJob = new CreateCollectionJob(collectionId, collectionName, indexNode, searchNodeListString, dataNodeListString);
			ResultFuture resultFuture = JobService.getInstance().offer(createCollectionJob);
			if (resultFuture != null) {
				Boolean result = (Boolean) resultFuture.take();
				if (result != null && result.booleanValue()) {
					isSuccess = true;
					Set<String> nodeIdSet = new HashSet<String>();
					nodeIdSet.add(indexNode);
					if (searchNodeListString != null) {
						for (String nodeStr : searchNodeListString.split(",")) {
							nodeStr = nodeStr.trim();
							if (nodeStr.length() > 0) {
								nodeIdSet.add(nodeStr);
							}
						}
					}
					if (dataNodeListString != null) {
						for (String nodeStr : dataNodeListString.split(",")) {
							nodeStr = nodeStr.trim();
							if (nodeStr.length() > 0) {
								nodeIdSet.add(nodeStr);
							}
						}
					}

					//자신을 제외하고 보낸다.
					nodeIdSet.remove(environment.myNodeId());
					NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
					NodeJobResult[] resultList = ClusterUtils.sendJobToNodeIdSet(createCollectionJob, nodeService, nodeIdSet, false);
					for (NodeJobResult r : resultList) {
						
						if(!r.isSuccess()) {
							isSuccess &= false;
							if(errorMessage.length() > 0){
								errorMessage += ", ";
							}
							errorMessage += (r.node() + ": collection create fail.");
						}
					}

				}
			}

		} catch (Throwable t) {
			errorMessage = t.getMessage();
		} finally {
			ResponseWriter responseWriter = getDefaultResponseWriter(response.getWriter());
			responseWriter.object();
			responseWriter.key("success").value(isSuccess);
			if (errorMessage != null) {
				responseWriter.key("errorMessage").value(errorMessage);
			}
			responseWriter.endObject();
			responseWriter.done();
		}

	}

}
