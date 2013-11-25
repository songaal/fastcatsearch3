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
import org.fastcatsearch.ir.config.ShardContext;
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
			 * shard별 색인파일 원격복사.
			 */
			indexingTaskState.setState(IndexingTaskState.STATE_FILECOPY);
			
			for(ShardContext shardContext : collectionContext.getShardContextList()){
				String shardId = shardContext.shardId();
				SegmentInfo segmentInfo = shardContext.dataInfo().getLastSegmentInfo();
				if(segmentInfo == null) {
					continue;
				}
				String segmentId = segmentInfo.getId();
				logger.debug("Transfer index data shard[{}] >> {}", shardId, segmentInfo);
				
				FilePaths shardIndexFilePaths = shardContext.indexFilePaths();
				File shardIndexDir = shardIndexFilePaths.file();
				File segmentDir = shardIndexFilePaths.file(segmentId);
				
				List<Node> nodeList = nodeService.getNodeById(shardContext.shardConfig().getDataNodeList());
				
				// 색인전송할디렉토리를 먼저 비우도록 요청.segmentDir
				File relativeDataDir = environment.filePaths().relativise(shardIndexDir);
				NodeDirectoryCleanJob cleanJob = new NodeDirectoryCleanJob(relativeDataDir);
				
				boolean nodeResult = ClusterUtils.sendJobToNodeList(cleanJob, nodeService, nodeList, false);
				if(!nodeResult){
					throw new FastcatSearchException("Node Index Directory Clean Failed! Dir=[{}]", segmentDir.getPath());
				}
				
				// 색인된 Segment 파일전송.
				IndexFileTransfer indexFileTransfer = new IndexFileTransfer(environment);
				indexFileTransfer.transferDirectory(segmentDir, nodeService, nodeList);
			
				/*
				 * 데이터노드에 컬렉션 리로드 요청.
				 */
				NodeCollectionReloadJob reloadJob = new NodeCollectionReloadJob(collectionContext);
				nodeResult = ClusterUtils.sendJobToNodeList(reloadJob, nodeService, nodeList, false);
				if(!nodeResult){
					throw new FastcatSearchException("Node Collection Reload Failed!");
				}
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
