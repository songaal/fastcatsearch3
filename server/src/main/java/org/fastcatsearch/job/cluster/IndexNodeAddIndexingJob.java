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

package org.fastcatsearch.job.cluster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.data.DataService;
import org.fastcatsearch.data.DataStrategy;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.CollectionIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.index.DeleteIdSet;
import org.fastcatsearch.ir.index.IndexWriteInfo;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.search.CollectionHandler;
import org.fastcatsearch.job.CacheServiceRestartJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.task.IndexFileTransfer;
import org.fastcatsearch.util.CollectionContextUtil;
import org.fastcatsearch.util.CollectionFilePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * */
public class IndexNodeAddIndexingJob extends StreamableJob {
	private static final long serialVersionUID = -4686760271693082945L;

	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

	private String collectionId;

	public IndexNodeAddIndexingJob() {
	}

	public IndexNodeAddIndexingJob(String collectionId) {
		this.collectionId = collectionId;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		try {
			
			long startTime = System.currentTimeMillis();
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			
			CollectionHandler collectionHandler = irService.collectionHandler(collectionId);
			if(collectionHandler == null){
				indexingLogger.error("[{}] CollectionHandler is not running!", collectionId);
				EventDBLogger.error(EventDBLogger.CATE_INDEX, "컬렉션 "+collectionId+"가 서비스중이 아님.");
				throw new FastcatSearchException("## ["+collectionId+"] CollectionHandler is not running...");
			}
			
			SegmentInfo currentSegmentInfo = collectionHandler.getLastSegmentReader().segmentInfo();
			if(currentSegmentInfo == null){
				indexingLogger.error("[{}] has no segment!  Do full-indexing first!!", collectionId);
				return null;
			}
			
			/*
			 * Do indexing!!
			 */
			//////////////////////////////////////////////////////////////////////////////////////////
			CollectionContext collectionContext = irService.collectionContext(collectionId).copy();
			CollectionIndexer collectionIndexer = new CollectionIndexer(collectionContext);
			SegmentInfo segmentInfo = collectionIndexer.addIndexing(collectionHandler);
			RevisionInfo revisionInfo = segmentInfo.getRevisionInfo();
			List<IndexWriteInfo> indexWriteInfoList = collectionIndexer.indexWriteInfoList();
			//////////////////////////////////////////////////////////////////////////////////////////
			
			//0보다 크면 revision이 증가된것이다.
			boolean revisionAppended = revisionInfo.getRevision() > 0;
			
			
			//status를 바꾸고 context를 저장한다.
			collectionContext.updateCollectionStatus(IndexingType.ADD, revisionInfo, startTime, System.currentTimeMillis());
			
			CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
			int dataSequence = collectionContext.getDataSequence();
			String segmentId = segmentInfo.getId();
			int revision = revisionInfo.getRevision();
			File collectionDataDir = collectionFilePaths.dataFile(dataSequence);
			File segmentDir = collectionContext.collectionFilePaths().segmentFile(dataSequence, segmentId);
			DeleteIdSet deleteIdSet = collectionIndexer.deleteIdSet();
			//먼저 업데이트를 해놔야 파일이 수정되서, 전송할수 있다. 
			collectionHandler.updateCollection(collectionContext, segmentInfo, segmentDir, deleteIdSet);
			
			File revisionDir = collectionFilePaths.revisionFile(dataSequence, segmentId, revision);
			
			
			/*
			 * 동기화 파일 생성. 
			 * 여기서는 1. segment/ 파일들에 덧붙일 정보들이 준비되어있어야한다. revision은 그대로 복사하므로 준비필요없음.
			 */
			File mirrorSyncFile = null;
			if(revisionAppended){
				mirrorSyncFile = collectionIndexer.createMirrorSyncFile(segmentDir, revisionDir);
			}
			
			
			/*
			 * 색인파일 원격복사.
			 */
			DataService dataService = ServiceManager.getInstance().getService(DataService.class);
			DataStrategy dataStrategy = dataService.getCollectionDataStrategy(collectionId);
			List<Node> nodeList = dataStrategy.dataNodes();
			if (nodeList == null || nodeList.size() == 0) {
				throw new FastcatSearchException("색인파일을 복사할 노드가 정의되어있지 않습니다.");
			}

//			String segmentId = segmentInfo.getId();
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
//			CollectionFilePaths collectionFilePaths = collectionContext.collectionFilePaths();
//			int dataSequence = collectionContext.getDataSequence();
//			File collectionDataDir = collectionFilePaths.dataFile(dataSequence);
//			File segmentDir = collectionFilePaths.segmentFile(dataSequence, segmentId);

			// 색인전송할디렉토리를 먼저 비우도록 요청.segmentDir
			File relativeDataDir = environment.filePaths().relativise(revisionDir);
			NodeDirectoryCleanJob cleanJob = new NodeDirectoryCleanJob(relativeDataDir);
			boolean nodeResult = sendJobToNodeList(cleanJob, nodeService, nodeList);
			if(!nodeResult){
				throw new FastcatSearchException("Node Index Directory Clean Failed! Dir=[{}]", segmentDir.getPath());
			}
			
			// 색인된 Segment 파일전송.
			IndexFileTransfer indexFileTransfer = new IndexFileTransfer(environment);
			//case 1. segment-append 파일과 revision/ 파일들을 전송한다.
			//case 2. 만약 segment가 생성된 경우라면 그대로 전송하면된다. 
			if(revisionAppended){
				indexFileTransfer.tranferRevision(collectionDataDir, segmentDir, revisionDir, nodeService, nodeList);
			}else{
				indexFileTransfer.tranferSegment(collectionDataDir, segmentDir, nodeService, nodeList);
			}
			
			/*
			 * 데이터노드에 컬렉션 리로드 요청.
			 */
			NodeSegmentUpdateJob updateJob = new NodeSegmentUpdateJob(collectionContext);
			nodeResult = sendJobToNodeList(updateJob, nodeService, nodeList);
			if(!nodeResult){
				throw new FastcatSearchException("Node Collection Reload Failed!");
			}
			
			
			
			int duration = (int) (System.currentTimeMillis() - startTime);
			
			/*
			 * 캐시 클리어.
			 */
			getJobExecutor().offer(new CacheServiceRestartJob());
			
			return new JobResult(new IndexingJobResult(collectionId, revisionInfo, duration));
			
		} catch (Exception e) {
			indexingLogger.error("[" + collectionId + "] Indexing error = " + e.getMessage(), e);
			throw new FastcatSearchException("ERR-00500", e, collectionId);
		}

	}
	
	private boolean sendJobToNodeList(Job job, NodeService nodeService, List<Node> nodeList) throws FastcatSearchException{
		List<ResultFuture> resultFutureList = new ArrayList<ResultFuture>(nodeList.size());
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			ResultFuture resultFuture = nodeService.sendRequest(node, job);
			resultFutureList.add(resultFuture);
		}
		for (int i = 0; i < resultFutureList.size(); i++) {
			Node node = nodeList.get(i);
			ResultFuture resultFuture = resultFutureList.get(i);
			Object obj = resultFuture.take();
			if(!resultFuture.isSuccess()){
				logger.debug("[{}] job 결과 : {}", node, obj);
//				throw new FastcatSearchException("작업실패 collection="+collectionId+", "+node);
				return false;
			}
		}
		return true;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
	}

}
