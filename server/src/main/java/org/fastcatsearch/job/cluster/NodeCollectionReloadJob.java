package org.fastcatsearch.job.cluster;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.ir.util.Counter;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableCollectionContext;
import org.fastcatsearch.util.CollectionContextUtil;

public class NodeCollectionReloadJob extends Job implements Streamable {

	private static final long serialVersionUID = -3825918267667946370L;
	
	private CollectionContext collectionContext;

	public NodeCollectionReloadJob() {
	}

	public NodeCollectionReloadJob(CollectionContext collectionContext) {
		this.collectionContext = collectionContext;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		try {
			ServiceManager serviceManager = ServiceManager.getInstance();
			IRService irService = serviceManager.getService(IRService.class);
			String collectionId = collectionContext.collectionId();
			NodeService nodeService = serviceManager.getService(NodeService.class);
			
			String myNodeId = nodeService.getMyNode().id();
			List<String> dataNodeList = collectionContext.collectionConfig().getDataNodeList();
			String indexNodeId = collectionContext.collectionConfig().getIndexNode();
			if(!dataNodeList.contains(myNodeId) && !(indexNodeId.equalsIgnoreCase(myNodeId))){
				//데이터 노드와 색인노드에 속하지 않았다면, 색인data가 없으므로, dataInfo를 비우고 저장한다. 
				collectionContext.clearDataInfoAndStatus();
			}
			
			CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
			CollectionHandler collectionHandler = irService.loadCollectionHandler(collectionId);
			logger.debug("== [{}] SegmentStatus ==", collectionId);
			collectionHandler.printSegmentStatus();
			logger.debug("===================");
			IndexStatus indexStatus = collectionContext.indexStatus().getFullIndexStatus();
			logger.debug("[{}] Collection Index Status > {}", collectionId, indexStatus);
			
			nodeService.updateLoadBalance(collectionId, dataNodeList);

			/*
			 * 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob());
			return new JobResult(true);

		} catch (Exception e) {
			logger.error("", e);
			throw new FastcatSearchException("ERR-00525", e);
		}

	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		StreamableCollectionContext streamableCollectionContext = new StreamableCollectionContext(environment);
		streamableCollectionContext.readFrom(input);
		this.collectionContext = streamableCollectionContext.collectionContext();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		StreamableCollectionContext streamableCollectionContext = new StreamableCollectionContext(collectionContext);
		streamableCollectionContext.writeTo(output);
	}

}
