package org.fastcatsearch.http.action.management.indexing;

import java.io.Writer;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.http.ActionAuthority;
import org.fastcatsearch.http.ActionAuthorityLevel;
import org.fastcatsearch.http.ActionMapping;
import org.fastcatsearch.http.action.ActionRequest;
import org.fastcatsearch.http.action.ActionResponse;
import org.fastcatsearch.http.action.AuthAction;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.indexing.CollectionIndexingStopJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.ResponseWriter;

@ActionMapping(value = "/indexing/stop", authority = ActionAuthority.Collections, authorityLevel = ActionAuthorityLevel.WRITABLE )
public class StopIndexingJobAction extends AuthAction {

	@Override
	public void doAuthAction(ActionRequest request, ActionResponse response) throws Exception {
		
		String collectionId = request.getParameter("collectionId");
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		String indexNodeId = collectionContext.collectionConfig().getIndexNode();
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node indexNode = nodeService.getNodeById(indexNodeId);
		
		CollectionIndexingStopJob collectionIndexingJob = new CollectionIndexingStopJob();
		collectionIndexingJob.setArgs(collectionId);
		
		ResultFuture jobResult = nodeService.sendRequest(indexNode, collectionIndexingJob);
		boolean isSuccess = false;
		if(jobResult != null){
			Object obj = jobResult.take();
			if(obj instanceof Boolean){
				isSuccess = (Boolean) obj;
			}
		}else{
			throw new FastcatSearchException("Cannot send indexing job.");
		}
		
		Writer writer = response.getWriter();
		ResponseWriter resultWriter = getDefaultResponseWriter(writer);
		
		resultWriter
		.object()
		.key("collectionId").value(collectionId)
		.key("success").value(isSuccess)
		.endObject();
		
		resultWriter.done();
		
	}

}
