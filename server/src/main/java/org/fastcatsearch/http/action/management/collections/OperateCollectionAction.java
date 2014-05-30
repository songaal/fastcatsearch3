package org.fastcatsearch.http.action.management.collections;

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
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.management.collections.OperateCollectionJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/management/collections/operate", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE)
public class OperateCollectionAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {

		String collectionId = request.getParameter("collectionId");
		String command = request.getParameter("command");

		boolean isSuccess = false;
		String errorMessage = null;

		try {
			OperateCollectionJob operateCollectionJob = new OperateCollectionJob(collectionId, command);
			
			IRService irService = ServiceManager.getInstance().getService(IRService.class);

			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
			
			if (collectionHandler == null) {
				errorMessage = "Collection [" + collectionId + "] is not exist.";
				return;
			}
			ResultFuture resultFuture = JobService.getInstance().offer(operateCollectionJob);
			Object object = resultFuture.take();
			if(object instanceof Boolean) {
				isSuccess = (Boolean) object;
			}
			Set<String> nodeIdSet = collectionHandler.collectionContext().collectionConfig().getCollectionNodeIDSet();
			nodeIdSet.remove(environment.myNodeId());
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			NodeJobResult[] resultList = ClusterUtils.sendJobToNodeIdSet(operateCollectionJob, nodeService, nodeIdSet, true);
			for(NodeJobResult r : resultList) {
				logger.debug("Operation {} >> {} : {}", command, r.node(), r.result());
				isSuccess = (isSuccess && r.isSuccess());
			}

		} catch (Exception e) {
			isSuccess = false;
			errorMessage = e.getMessage();
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
