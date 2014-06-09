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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fastcatsearch.cluster.ClusterUtils;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeJobResult;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
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
import org.fastcatsearch.job.cluster.NodeCollectionReloadJob;
import org.fastcatsearch.job.cluster.NodeDirectoryCleanJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.service.ServiceManager;
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
		
		prepare(IndexingType.ADD, "ALL");
		
		Throwable throwable = null;
		ResultStatus resultStatus = ResultStatus.RUNNING;
		Object result = null;
		long startTime = System.currentTimeMillis();
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
			
			if(!updateIndexingStatusStart()) {
				logger.error("Cannot start indexing job. {} : {}", collectionId, indexNodeId);
				resultStatus = ResultStatus.CANCEL;
				return new JobResult();
			}

			//증분색인은 collection handler자체를 수정하므로 copy하지 않는다.
//			collectionContext = collectionContext.copy();
			/*
			 * Do indexing!!
			 */
			//////////////////////////////////////////////////////////////////////////////////////////
			SegmentInfo lastSegmentInfo = collectionContext.dataInfo().getLastSegmentInfo();
			if(lastSegmentInfo == null){
				//색인이 안된상태이다.
				throw new FastcatSearchException("Cannot index collection. It has no full indexing information. collectionId = "+collectionContext.collectionId());
			}
			String lastRevisionUUID = lastSegmentInfo.getRevisionInfo().getUuid();
			
			boolean isIndexed = false;
			CollectionAddIndexer collectionIndexer = new CollectionAddIndexer(collectionHandler);
			indexer = collectionIndexer;
			collectionIndexer.setTaskState(indexingTaskState);
			Throwable indexingThrowable = null;
			try {
				indexer.doIndexing();
			}catch(Throwable e){
				indexingThrowable = e;
			} finally {
				if (collectionIndexer != null) {
					try {
						isIndexed = collectionIndexer.close();
					} catch (Throwable closeThrowable) {
						// 이전에 이미 발생한 에러가 있다면 close 중에 발생한 에러보다 이전 에러를 throw한다.
						if (indexingThrowable == null) {
							indexingThrowable = closeThrowable;
						}
					}
				}
				if(indexingThrowable != null){
					throw indexingThrowable;
				}
			}
			if(!isIndexed && stopRequested){
				//여기서 끝낸다.
				throw new IndexingStopException();
			}
			
			/*
			 * 색인파일 원격복사.
			 */
			indexingTaskState.setStep(IndexingTaskState.STEP_FILECOPY);
			
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
				
				/*
				 * 색인파일 원격복사.
				 */
				List<Node> nodeList = new ArrayList<Node>(nodeService.getNodeById(collectionContext.collectionConfig().getDataNodeList()));
				//색인노드가 data node에 추가되어있다면 제거한다.
				nodeList.remove(nodeService.getMyNode());
				
				if(nodeList.size() > 0) {
					//색인노드만 있어도 OK.
					
					/*
					 * lastRevisionUUID가 일치하는 보낼노드가 존재하는지 확인한다.
					 * 존재한다면 mirror sync file을 만든다.
					 * 
					 * */
					NodeJobResult[] nodeResultList = null;
					GetCollectionIndexRevisionUUIDJob getRevisionUUIDJob = new GetCollectionIndexRevisionUUIDJob();
					getRevisionUUIDJob.setArgs(collectionId);
					nodeResultList = ClusterUtils.sendJobToNodeList(getRevisionUUIDJob, nodeService, nodeList, false);
					
					//성공한 node만 전송.
					nodeList = new ArrayList<Node>();
					for (int i = 0; i < nodeResultList.length; i++) {
						NodeJobResult r = nodeResultList[i];
						logger.debug("node#{} >> {}", i, r);
						if (r.isSuccess()) {
							String uuid = (String) r.result();
							if(lastRevisionUUID.equals(uuid)){
								nodeList.add(r.node());
							}else{
								logger.error("{} has different uuid > {}", r.node(), uuid);
							}
						}else{
							logger.warn("Cannot get revision information > {}", r.node());
						}
					}
					
					
					
					//TODO indexWriteInfoList를 파일로 저장해놓아야 실패한 노드에 차후 전송이 가능하게 된다. 또는 mirror sync파일을 매번 만들어 놓는다던지.. 
					//revision이 여러번 바뀌면 mirror sync를 여러번전송해서 한번씩 업데이트. 
					
					
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
					
					
					if(stopRequested){
						throw new IndexingStopException();
					}
					
					// 색인전송할디렉토리를 먼저 비우도록 요청.segmentDir
					File relativeDataDir = environment.filePaths().relativise(transferDir);
					NodeDirectoryCleanJob cleanJob = new NodeDirectoryCleanJob(relativeDataDir);
					nodeResultList = ClusterUtils.sendJobToNodeList(cleanJob, nodeService, nodeList, false);
					
					//성공한 node만 전송.
					nodeList = new ArrayList<Node>();
					for (int i = 0; i < nodeResultList.length; i++) {
						NodeJobResult r = nodeResultList[i];
						logger.debug("node#{} >> {}", i, r);
						if (r.isSuccess()) {
							nodeList.add(r.node());
						}else{
							logger.warn("Do not send index file to {}", r.node());
						}
					}
					// 색인된 Segment 파일전송.
					//case 1. segment-append 파일과 revision/ 파일들을 전송한다.
					//case 2. 만약 segment가 생성 or 수정된 경우라면 그대로 전송하면된다. 
					
					TransferIndexFileMultiNodeJob transferJob = new TransferIndexFileMultiNodeJob(transferDir, nodeList);
					ResultFuture resultFuture = JobService.getInstance().offer(transferJob);
					Object obj = resultFuture.take();
					if(resultFuture.isSuccess() && obj != null){
						nodeResultList = (NodeJobResult[]) obj;
					}
					//성공한 node만 전송.
					nodeList = new ArrayList<Node>();
					for (int i = 0; i < nodeResultList.length; i++) {
						NodeJobResult r = nodeResultList[i];
						logger.debug("node#{} >> {}", i, r);
						if (r.isSuccess()) {
							nodeList.add(r.node());
						}else{
							logger.warn("Do not reload at {}", r.node());
						}
					}
					
					if(stopRequested){
						throw new IndexingStopException();
					}
					
					Set<Node> reloadNodeSet = new HashSet<Node>();
					//데이터노드
					reloadNodeSet.addAll(nodeList);
					//인덱스노드
					reloadNodeSet.add(indexNode);
					//마스터노드 (관리도구에 보여지기 위함) 추가
					reloadNodeSet.add(nodeService.getMasterNode());
					// 
					for(String nodeId : collectionContext.collectionConfig().getSearchNodeList()){
						reloadNodeSet.add(nodeService.getNodeById(nodeId));
					}
					
					/*
					 * 데이터노드에 컬렉션 리로드 요청.
					 */
					NodeCollectionReloadJob reloadJob = new NodeCollectionReloadJob(collectionContext);
					nodeResultList = ClusterUtils.sendJobToNodeList(reloadJob, nodeService, nodeList, false);
					for (int i = 0; i < nodeResultList.length; i++) {
						NodeJobResult r = nodeResultList[i];
						logger.debug("node#{} >> {}", i, r);
						if (r.isSuccess()) {
							logger.info("{} Collection reload OK.", r.node());
						}else{
							logger.warn("{} Collection reload Fail.", r.node());
						}
					}
				}
			}
			
			indexingTaskState.setStep(IndexingTaskState.STEP_FINALIZE);
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
			indexingTaskState.setStep(IndexingTaskState.STEP_END);
			return new JobResult(result);
			
		} catch (IndexingStopException e){
			if(stopRequested){
				resultStatus = ResultStatus.STOP;
			}else{
				resultStatus = ResultStatus.CANCEL;
			}
			result = new IndexingJobResult(collectionId, null, (int) (System.currentTimeMillis() - startTime), false);
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
