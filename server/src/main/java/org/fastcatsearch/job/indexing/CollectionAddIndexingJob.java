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
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.CollectionAddIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.MirrorSynchronizer;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.index.IndexWriteInfoList;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.cluster.NodeDirectoryCleanJob;
import org.fastcatsearch.job.cluster.NodeSegmentUpdateJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.task.IndexFileTransfer;
import org.fastcatsearch.transport.vo.StreamableThrowable;

/**
 * 특정 collection의 index node에서 수행되는 job.
 * index node가 아닌 노드에 전달되면 색인을 수행하지 않는다.
 *  
 * */
public class CollectionAddIndexingJob extends IndexingJob {

	private static final long serialVersionUID = 7898036370433248984L;

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		prepare(IndexingType.ADD);
		
		Throwable throwable = null;
		ResultStatus resultStatus = ResultStatus.RUNNING;
		Object result = null;
		
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			//find index node
			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
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

			//증분색인은 collection handler자체를 수정하므로 copy하지 않는다.
//			collectionContext = collectionContext.copy();
			/*
			 * Do indexing!!
			 */
			//////////////////////////////////////////////////////////////////////////////////////////
			
			CollectionAddIndexer collectionIndexer = new CollectionAddIndexer(collectionHandler);
			collectionIndexer.setState(indexingTaskState);
			collectionIndexer.doIndexing();
			boolean isIndexed = collectionIndexer.close();
			if(!isIndexed){
				//여기서 끝낸다.
				resultStatus = ResultStatus.CANCEL;
				result = new IndexingJobResult(collectionId, null, (int) (System.currentTimeMillis() - startTime));
				return new JobResult(result);
			}
			/*
			 * 색인파일 원격복사.
			 */
			indexingTaskState.setState(IndexingTaskState.STATE_FILECOPY);
			
			IndexWriteInfoList indexWriteInfoList = collectionIndexer.indexWriteInfoList();
			SegmentInfo segmentInfo = collectionContext.dataInfo().getLastSegmentInfo();
			if(segmentInfo != null) {
				
				String segmentId = segmentInfo.getId();
				logger.debug("Transfer index data collection[{}] >> {}", collectionId, segmentInfo);
				RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
				int revisionId = revisionInfo.getId();
				int dataSequence = collectionContext.getIndexSequence();
				File revisionDir = collectionContext.collectionFilePaths().dataPaths().revisionFile(dataSequence, segmentId, revisionId);
				File segmentDir = collectionContext.collectionFilePaths().dataPaths().segmentFile(dataSequence, segmentId);
				
				File transferDir = null;
				/*
				 * 동기화 파일 생성. 
				 * 여기서는 1. segment/ 파일들에 덧붙일 정보들이 준비되어있어야한다. revision은 그대로 복사하므로 준비필요없음.
				 */
				//0보다 크면 revision이 증가된것이다.
				boolean revisionAppended = revisionInfo.getId() > 0;
				boolean revisionHasInserts = revisionInfo.getInsertCount() > 0;
				File mirrorSyncFile = null;
				if(revisionAppended){
					if(revisionHasInserts){
						mirrorSyncFile = new MirrorSynchronizer().createMirrorSyncFile(indexWriteInfoList, revisionDir);
						logger.debug("동기화 파일 생성 >> {}", mirrorSyncFile.getAbsolutePath());
					}
					
					transferDir = revisionDir;
				}else{
					//세그먼트 전체전송.
					transferDir = segmentDir;
					logger.debug("세그먼트 생성되어 segment dir 전송필요");
				}
				
				
				/*
				 * 색인파일 원격복사.
				 */
				List<Node> nodeList = nodeService.getNodeById(collectionContext.collectionConfig().getDataNodeList());
				if (nodeList == null || nodeList.size() == 0) {
					throw new FastcatSearchException("색인파일을 복사할 노드가 정의되어있지 않습니다.");
				}
				
				// 색인전송할디렉토리를 먼저 비우도록 요청.segmentDir
				File relativeDataDir = environment.filePaths().relativise(transferDir);
				NodeDirectoryCleanJob cleanJob = new NodeDirectoryCleanJob(relativeDataDir);
				boolean[] nodeResultList = ClusterUtils.sendJobToNodeList(cleanJob, nodeService, nodeList, false);
				
				for(int i=0;i<nodeResultList.length; i++){
					boolean r = nodeResultList[i];
					logger.debug("node#{} >> {}", i, r);
				}
				
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
				//case 1. segment-append 파일과 revision/ 파일들을 전송한다.
				//case 2. 만약 segment가 생성 or 수정된 경우라면 그대로 전송하면된다. 
				nodeResultList = indexFileTransfer.transferDirectory(transferDir, nodeService, nodeList2);
				
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
				NodeSegmentUpdateJob reloadJob = new NodeSegmentUpdateJob(collectionContext);
				nodeResultList = ClusterUtils.sendJobToNodeList(reloadJob, nodeService, nodeList3, false);
				for (int i = 0; i < nodeResultList.length; i++) {
					if (nodeResultList[i]) {
						logger.info("{} Collection reload OK.", nodeList3.get(i));
					}else{
						logger.warn("{} Collection reload Fail.", nodeList3.get(i));
					}
				}
			}
			
			indexingTaskState.setState(IndexingTaskState.STATE_FINALIZE);
			int duration = (int) (System.currentTimeMillis() - startTime);
			
			/*
			 * 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob());

			IndexStatus indexStatus = collectionContext.indexStatus().getAddIndexStatus();
			indexingLogger.info("[{}] Collection Add Indexing Finished! {} time = {}", collectionId, indexStatus, duration);
			logger.info("== SegmentStatus ==");
			collectionHandler.printSegmentStatus();
			logger.info("===================");
			
			result = new IndexingJobResult(collectionId, indexStatus, duration);
			resultStatus = ResultStatus.SUCCESS;

			return new JobResult(result);

		} catch (Throwable e) {
			indexingLogger.error("[" + collectionId + "] Indexing", e);
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
