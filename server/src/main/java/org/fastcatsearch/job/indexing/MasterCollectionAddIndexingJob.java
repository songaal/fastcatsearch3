package org.fastcatsearch.job.indexing;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.service.ServiceManager;

public class MasterCollectionAddIndexingJob extends MasterNodeJob {

	private static final long serialVersionUID = -9030366773507675894L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		String collectionId = getStringArgs();
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		String indexNodeId = collectionContext.collectionConfig().getIndexNode();
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node indexNode = nodeService.getNodeById(indexNodeId);
		
		CollectionAddIndexingJob collectionIndexingJob = new CollectionAddIndexingJob();
		collectionIndexingJob.setArgs(collectionId);
		logger.info("Request add indexing job to index node[{}] >> {}", indexNodeId, indexNode);
		ResultFuture jobResult = nodeService.sendRequest(indexNode, collectionIndexingJob);
		if(jobResult != null){
			Object obj = jobResult.take();
		}else{
			throw new FastcatSearchException("Cannot send indexing job of {} to {}", collectionId, indexNodeId);
		}
			
			
		return new JobResult();
	}

}
