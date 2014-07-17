package org.fastcatsearch.job.indexing;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.service.ServiceManager;

public class MasterCollectionIndexBuildFullIndexingJob extends MasterNodeJob {

	private static final long serialVersionUID = -9030366773507675894L;

	@Override
	public JobResult doRun() throws FastcatSearchException {

		String collectionId = getStringArgs();

		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		if(collectionContext == null) {
			throw new FastcatSearchException("Collection [" + collectionId + "] is not exist.");
		}
		String indexNodeId = collectionContext.collectionConfig().getIndexNode();

		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node indexNode = nodeService.getNodeById(indexNodeId);

		// 전체색인용 context를 준비한다.
		CollectionContext newCollectionContext = collectionContext.copy();
		if (newCollectionContext.workSchemaSetting() != null) {
			newCollectionContext.setSchema(new Schema(newCollectionContext.workSchemaSetting()));
		}
		CollectionIndexBuildFullIndexingJob documentStoreJob = new CollectionIndexBuildFullIndexingJob(newCollectionContext);
		documentStoreJob.setArgs(collectionId);

		ResultFuture jobResult = nodeService.sendRequest(indexNode, documentStoreJob);
		if (jobResult != null) {
			Object obj = jobResult.take();
			logger.debug("CollectionIndexBuildFullIndexingJob result = {}", obj);
			if (obj != null && obj instanceof IndexingJobResult) {
				IndexingJobResult indexingJobResult = (IndexingJobResult) obj;
				if (indexingJobResult.isSuccess) {
					//do nothing
				}
			}

		} else {
			throw new FastcatSearchException("Cannot send indexing job.");
		}

		return new JobResult();
	}

}
