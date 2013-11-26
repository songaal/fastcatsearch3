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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.CollectionFullIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.cluster.NodeCollectionReloadJob;
import org.fastcatsearch.job.cluster.NodeDirectoryCleanJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.task.IndexFileTransfer;
import org.fastcatsearch.transport.vo.StreamableThrowable;
import org.fastcatsearch.util.FilePaths;

/**
 * 특정 collection의 index node에서 수행되는 job.
 * index node가 아닌 노드에 전달되면 색인을 수행하지 않는다.
 *  
 * */
public class CollectionFullIndexingJob extends IndexingJob {

	private static final long serialVersionUID = 7898036370433248984L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		prepare(IndexingType.FULL);
		
		Throwable throwable = null;
		boolean isSuccess = false;
		Object result = null;
		
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			//find index node
			CollectionContext collectionContext = irService.collectionContext(collectionId);
			String indexNodeId = collectionContext.collectionConfig().getIndexNode();
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			Node indexNode = nodeService.getNodeById(indexNodeId);
			
			if(!nodeService.isMyNode(indexNode)){
				//Pass job to index node
//				nodeService.sendRequest(indexNode, this);
				//작업수행하지 않음.
				throw new RuntimeException("Invalid index node collection[" + collectionId + "] node[" + indexNodeId + "]");
			}
			
			long startTime = System.currentTimeMillis();
			
			

			updateIndexingStatusStart();

			collectionContext = collectionContext.copy();
			/*
			 * Do indexing!!
			 */
			//////////////////////////////////////////////////////////////////////////////////////////
			
			CollectionFullIndexer collectionFullIndexer = new CollectionFullIndexer(collectionContext);
			collectionFullIndexer.setState(indexingTaskState);
			collectionFullIndexer.doIndexing();
			collectionFullIndexer.close();
			
			/*
			 * 색인파일 원격복사.
			 */
			indexingTaskState.setState(IndexingTaskState.STATE_FILECOPY);
			
			SegmentInfo segmentInfo = collectionContext.dataInfo().getLastSegmentInfo();
			if (segmentInfo != null) {
				String segmentId = segmentInfo.getId();
				logger.debug("Transfer index data collection[{}] >> {}", collectionId, segmentInfo);

				FilePaths indexFilePaths = collectionContext.indexFilePaths();
				File indexDir = indexFilePaths.file();
				File segmentDir = indexFilePaths.file(segmentId);

				List<Node> nodeList = nodeService.getNodeById(collectionContext.collectionConfig().getDataNodeList());

				// 색인전송할디렉토리를 먼저 비우도록 요청.segmentDir
				File relativeDataDir = environment.filePaths().relativise(indexDir);
				NodeDirectoryCleanJob cleanJob = new NodeDirectoryCleanJob(relativeDataDir);

				boolean[] nodeResultList = ClusterUtils.sendJobToNodeList(cleanJob, nodeService, nodeList, false);
				
				for(int i=0;i<nodeResultList.length; i++){
					boolean r = nodeResultList[i];
					logger.debug("node#{}1 >> {}", i, r);
				}
//				if (!nodeResult) {
//					throw new FastcatSearchException("Node Index Directory Clean Failed! Dir=[{}]", segmentDir.getPath());
//				}

				//성공한 node만 전송.
				List<Node> nodeList2 = new ArrayList<Node>();
				for (int i = 0; i < nodeResultList.length; i++) {
					if (nodeResultList[i]) {
						nodeList2.add(nodeList.get(i));
					}else{
						logger.warn("Do not send index file to {}", nodeList.get(i));
					}
				}
				// 색인된 Segment 파일전송.
				IndexFileTransfer indexFileTransfer = new IndexFileTransfer(environment);
				nodeResultList = indexFileTransfer.transferDirectory(segmentDir, nodeService, nodeList2);
				//성공한 node만 전송.
				List<Node> nodeList3 = new ArrayList<Node>();
				for (int i = 0; i < nodeResultList.length; i++) {
					if (nodeResultList[i]) {
						nodeList3.add(nodeList2.get(i));
					}else{
						logger.warn("Do not reload at {}", nodeList2.get(i));
					}
				}
				
				/*
				 * 데이터노드에 컬렉션 리로드 요청.
				 */
				NodeCollectionReloadJob reloadJob = new NodeCollectionReloadJob(collectionContext);
				nodeResultList = ClusterUtils.sendJobToNodeList(reloadJob, nodeService, nodeList3, false);
				for (int i = 0; i < nodeResultList.length; i++) {
					if (nodeResultList[i]) {
						logger.info("{} Collection reload OK.", nodeList3.get(i));
					}else{
						logger.warn("{} Collection reload Fail.", nodeList3.get(i));
					}
				}
				
//				if (!nodeResult) {
//					throw new FastcatSearchException("Node Collection Reload Failed!");
//				}
			}
			/*
			 * 데이터노드가 리로드 완료되었으면 인덱스노드도 리로드 시작.
			 * */
			indexingTaskState.setState(IndexingTaskState.STATE_FINALIZE);
			
			CollectionHandler collectionHandler = irService.loadCollectionHandler(collectionContext);
			CollectionHandler oldCollectionHandler = irService.putCollectionHandler(collectionId, collectionHandler);
			if (oldCollectionHandler != null) {
				logger.info("## [{}] Close Previous Collection Handler", collectionContext.collectionId());
				oldCollectionHandler.close();
			}
			
			int duration = (int) (System.currentTimeMillis() - startTime);
			
			/*
			 * 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob());

			IndexStatus indexStatus = collectionContext.indexStatus().getFullIndexStatus();
			indexingLogger.info("[{}] Collection Full Indexing Finished! {} time = {}", collectionId, indexStatus, duration);
			logger.info("== SegmentStatus ==");
			collectionHandler.printSegmentStatus();
			logger.info("===================");
			
			result = new IndexingJobResult(collectionId, indexStatus, duration);
			isSuccess = true;

			return new JobResult(result);

		} catch (Throwable e) {
			indexingLogger.error("[" + collectionId + "] Indexing", e);
			throwable = e;
			throw new FastcatSearchException("ERR-00500", throwable, collectionId); // 전체색인실패.
		} finally {
			Streamable streamableResult = null;
			if (throwable != null) {
				streamableResult = new StreamableThrowable(throwable);
			} else if (result instanceof IndexingJobResult) {
				streamableResult = (IndexingJobResult) result;
			}

			updateIndexingStatusFinish(isSuccess, streamableResult);
		}

	}

}
