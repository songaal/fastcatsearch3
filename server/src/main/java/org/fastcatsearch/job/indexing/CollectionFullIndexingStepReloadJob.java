package org.fastcatsearch.job.indexing;

import java.util.Set;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.job.cluster.NodeCollectionReloadJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.node.ReloadNode;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class CollectionFullIndexingStepReloadJob extends IndexingJob {

	private static final long serialVersionUID = 7898036370433248984L;
	private CollectionContext collectionContext;

	public CollectionFullIndexingStepReloadJob(){
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		// TODO Auto-generated method stub
		prepare(IndexingType.FULL, "RELOAD-INDEX");

		NodeJobResult[] nodeResultList = null;		
		Throwable throwable = null;

		ResultStatus resultStatus = ResultStatus.RUNNING;

		Object result = null;
		long startTime = System.currentTimeMillis();

		try {
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			ReloadNode reloadNode = ReloadNode.getInstance();

			if(reloadNode.isNullCheck(collectionId)){ // true : null, false : not null
				throw new RuntimeException("reload-indexing fail.");
			}

			if(!updateIndexingStatusStart()){
				resultStatus = ResultStatus.CANCEL;

				return new JobResult();
			}

			indexingTaskState.setStep(IndexingTaskState.STEP_RELOAD);

			collectionContext = reloadNode.getCollectionContext().get(collectionId);

			logger.debug(">> id_1 : {}, id_2 : {}", collectionContext.collectionId(), collectionId);	// collectionId : 파라미터로 넘어도 id값.

			/*
			 * 데이터노드에 컬렉션 리로드 요청.
			 */	
			NodeCollectionReloadJob reloadJob = new NodeCollectionReloadJob(collectionContext);

			Set<Node> nodeSet = reloadNode.getCollectionCopyNode().get(collectionId);

			if(nodeSet == null){
				logger.error("{} nodeSet null", collectionId);
			}
			else{
				logger.error("{} nodeSet not null", collectionId);
			}

			nodeResultList = ClusterUtils.sendJobToNodeSet(reloadJob, nodeService, nodeSet, true);
			for (int i = 0; i < nodeResultList.length; i++) {
				NodeJobResult r = nodeResultList[i];
				logger.debug("node#{} >> {}", i, r);
				if (r.isSuccess()) {
					logger.info("{} Collection reload OK.", r.node());
				}else{
					logger.warn("{} Collection reload Fail.", r.node());
				}
			}

			if(indexingTaskState == null){
				logger.info("reloadJob indexingTaskState null");
			}
			else{
				logger.info("reloadJob indexingTaskState not null");
			}

			indexingTaskState.setStep(IndexingTaskState.STEP_FINALIZE);
			int duration = (int) (System.currentTimeMillis() - startTime);

			IndexStatus indexStatus = collectionContext.indexStatus().getFullIndexStatus();
			indexingLogger.info("[{}] ! time = {}", collectionId, duration);

			result = new IndexingJobResult(collectionId, indexStatus, duration);
			resultStatus = ResultStatus.SUCCESS;
			indexingTaskState.setStep(IndexingTaskState.STEP_END);

			reloadNode.init(collectionId);

			return new JobResult(result);
		} catch (Throwable e) {
			// TODO: handle exception
			indexingLogger.error("[" + collectionId + "] Indexing", e);
			throwable = e;
			resultStatus = ResultStatus.FAIL;

			throw new FastcatSearchException("ERR-00500", throwable, collectionId); // 전체색인실패.
		}finally {
			Streamable streamableResult = null;
			if (throwable != null) {
				streamableResult = new StreamableThrowable(throwable);
			} else if (result instanceof Streamable) {
				streamableResult = (Streamable) result;
			}

			updateIndexingStatusFinish(resultStatus, streamableResult);
		}
	}
}
