package org.fastcatsearch.job.indexing;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.job.MasterNodeJob;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.util.CollectionContextUtil;

public class MasterCollectionFullIndexingJob extends MasterNodeJob {

	private static final long serialVersionUID = -9030366773507675894L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		String collectionId = getStringArgs();
		
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		CollectionContext collectionContext = irService.collectionContext(collectionId);
		String indexNodeId = collectionContext.collectionConfig().getIndexNode();
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node indexNode = nodeService.getNodeById(indexNodeId);
		
//		//master가 index 노드인지.
//		if(!nodeService.isMyNode(indexNode)){
//			BeforeFullIndexingNodeUpdateJob beforeJob = new BeforeFullIndexingNodeUpdateJob(collectionContext);
//			ResultFuture jobResult = nodeService.sendRequest(indexNode, beforeJob);
//			if(jobResult != null){
//				Object obj = jobResult.take();
//			}else{
//				throw new FastcatSearchException("Cannot update index node config.");
//			}
//		}
		
		//전체색인용 context를 준비한다.
		CollectionContext newCollectionContext = collectionContext.copy();
		if(newCollectionContext.workSchemaSetting() != null){
			newCollectionContext.setSchema(new Schema(newCollectionContext.workSchemaSetting()));
		}
		
		CollectionFullIndexingJob collectionIndexingJob = new CollectionFullIndexingJob(newCollectionContext);
		collectionIndexingJob.setArgs(collectionId);
		
		ResultFuture jobResult = nodeService.sendRequest(indexNode, collectionIndexingJob);
		if(jobResult != null){
			Object obj = jobResult.take();
			
			try {
				CollectionContextUtil.saveCollectionAfterIndexing(newCollectionContext);
			} catch (SettingException e) {
				throw new FastcatSearchException(e);
			}		
			
			
		}else{
			throw new FastcatSearchException("Cannot send indexing job.");
		}
			
			
		return new JobResult();
	}

}
