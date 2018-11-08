package org.fastcatsearch.job.indexing;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.service.ServiceManager;

public class MasterCollectionFullIndexingStepReloadJob extends MasterNodeJob {

	private static final long serialVersionUID = -9030366773507675894L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		// TODO Auto-generated method stub
		String collectionId = getStringArgs();

		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);

		if(collectionContext == null) {
			throw new FastcatSearchException("Reload collection [" + collectionId + "] is not exist.");
		}

		String indexNodeId = collectionContext.collectionConfig().getIndexNode();

		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node indexNode = nodeService.getNodeById(indexNodeId);

		CollectionFullIndexingStepReloadJob fullIndexingStepReloadJob = new CollectionFullIndexingStepReloadJob();
		fullIndexingStepReloadJob.setArgs(collectionId);
		fullIndexingStepReloadJob.setScheduled(isScheduled);

		logger.info("Request reload step job to index node[{}] >> {}, isScheduled={}", indexNodeId, indexNode, isScheduled);

		ResultFuture jobResult = nodeService.sendRequest(indexNode, fullIndexingStepReloadJob);

		if (jobResult != null) {
			Object obj = jobResult.take();

			logger.debug("ReloadStepJob result = {}", obj);
		} else {
			logger.debug("Cannot send indexing job of "+collectionId+" to "+indexNodeId);
		}

		return new JobResult();
	}

}
