/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.job.indexing;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.cluster.NodeCollectionMergingJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;

import java.util.ArrayList;
import java.util.List;

/**
 *  세그먼트들의 상태를 보고 머징이 필요한지 판단하여, 머징을 수행한다.
 *  인덱스노드에서 수행된다.
 * */
public class CollectionMergingJob extends IndexingJob {

	private static final long serialVersionUID = 7898036370433248984L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		prepare(IndexingType.MERGE, "ALL");
		
		Throwable throwable = null;
		ResultStatus resultStatus = ResultStatus.RUNNING;
		Object result = null;
		long startTime = System.currentTimeMillis();
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			//find index node
			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			if(collectionContext == null) {
				throw new FastcatSearchException("Collection [" + collectionId + "] is not exist.");
			}
			String indexNodeId = collectionContext.collectionConfig().getIndexNode();
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			Node indexNode = nodeService.getNodeById(indexNodeId);
			
			if(!nodeService.isMyNode(indexNode)){
				//작업수행하지 않음.
				throw new RuntimeException("Invalid index node collection[" + collectionId + "] node[" + indexNodeId + "]");
			}
			
			if(!updateIndexingStatusStart()) {
				logger.error("Cannot start merging job. {} : {}", collectionId, indexNodeId);
				resultStatus = ResultStatus.CANCEL;
				return new JobResult();
			}

            List<Node> nodeList = new ArrayList<Node>(nodeService.getNodeById(collectionContext.collectionConfig().getDataNodeList()));
            //색인노드가 data node에 추가되어있다면 제거한다.
            nodeList.remove(nodeService.getMyNode());
            NodeCollectionMergingJob nodeCollectionMergingJob = new NodeCollectionMergingJob();
            NodeJobResult[] nodeResultList = null;
            if(nodeList.size() > 0) {
                nodeResultList = ClusterUtils.sendJobToNodeList(nodeCollectionMergingJob, nodeService, nodeList, false);
                for(NodeJobResult nodeJobResult : nodeResultList) {
                    //성공하면 나도한다.
                    nodeJobResult.result();
                }
            }
            ResultFuture myResult = getJobExecutor().offer(nodeCollectionMergingJob);
            Object resultObj = myResult.take();

			indexingTaskState.setStep(IndexingTaskState.STEP_FINALIZE);
			int duration = (int) (System.currentTimeMillis() - startTime);


            //FIXME
			indexingLogger.info("[{}] Collection Merging Finished! time = {}", collectionId, duration);
			logger.info("== SegmentStatus ==");
			collectionHandler.printSegmentStatus();
			logger.info("===================");
			resultStatus = ResultStatus.SUCCESS;
			indexingTaskState.setStep(IndexingTaskState.STEP_END);
			return new JobResult(true);
			
		} catch (Throwable e) {
			indexingLogger.error("[" + collectionId + "] Merging", e);
			throwable = e;
			resultStatus = ResultStatus.FAIL;
			throw new FastcatSearchException("ERR-00501", throwable, collectionId); // 색인실패.
		} finally {
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
