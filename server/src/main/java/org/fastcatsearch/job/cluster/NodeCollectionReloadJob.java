package org.fastcatsearch.job.cluster;

import java.io.IOException;
import java.util.List;

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
			
			
			List<String> dataNodeList = collectionContext.collectionConfig().getDataNodeList();
			
			//데이터노드만 업데이트 한다.
			String myNodeId = nodeService.getMyNode().id();
			if (dataNodeList.contains(myNodeId) || 
					collectionContext.collectionConfig().getIndexNode().equals(myNodeId)) {
				CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
				
			} else {
				//TODO:새로만든 컬렉션인 경우, 혹은 컬렉션 디렉토리가 없는 경우에만 저장하도록 한다.
				//단. 데이터를 로딩하지 않기 위해 색인상태는 모두 지운다.
				if(!collectionContext.dataFilePaths().dataFile().exists()) {
					collectionContext.clearDataInfoAndStatus();
					CollectionContextUtil.saveCollectionAfterIndexing(collectionContext);
				}
			}
			
			CollectionHandler collectionHandler = irService.loadCollectionHandler(collectionContext);
			Counter queryCounter = irService.queryCountModule().getQueryCounter(collectionId);
			collectionHandler.setQueryCounter(queryCounter);
			
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, collectionHandler);
			if (oldCollectionHandler != null) {
				logger.info("## [{}] Close Previous Collection Handler", collectionId);
				oldCollectionHandler.close();
			}
			
			logger.info("== [{}] SegmentStatus ==", collectionId);
			collectionHandler.printSegmentStatus();
			logger.info("===================");
			IndexStatus indexStatus = collectionContext.indexStatus().getFullIndexStatus();
			logger.info("[{}] Collection Index Status > {}", collectionId, indexStatus);

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
