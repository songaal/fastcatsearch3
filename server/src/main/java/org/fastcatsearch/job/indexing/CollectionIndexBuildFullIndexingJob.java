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

import java.io.IOException;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.CollectionIndexBuildIndexer;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.index.SelectedIndexList;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableCollectionContext;
import org.fastcatsearch.transport.vo.StreamableThrowable;

/**
 * 전체색인용 색인만 생성한다. 문서는 저장하지 않고, 이미 저장된 문서를 읽어서 색인한다.
 * 
 * index node가 아닌 노드에 전달되면 색인을 수행하지 않는다.
 *  
 * */
public class CollectionIndexBuildFullIndexingJob extends IndexingJob {

	private static final long serialVersionUID = -4291415269961866851L;
	private CollectionContext collectionContext; 
	
	public CollectionIndexBuildFullIndexingJob(){
	}
	
	public CollectionIndexBuildFullIndexingJob(CollectionContext collectionContext){
		this.collectionContext = collectionContext;
	}
	
	@Override
	public void requestStop(){
		logger.info("Collection [{}] Document Store Job Stop Requested! ", collectionId);
		stopRequested = true;
		if(indexer != null){
			indexer.requestStop();
		}
		indexingTaskState.addState(IndexingTaskState.STATE_STOP_REQUESTED);
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		prepare(IndexingType.FULL_INDEX_BUILD);
		
		
		Throwable throwable = null;
		ResultStatus resultStatus = ResultStatus.RUNNING;
		Object result = null;
		long startTime = System.currentTimeMillis();
		try {
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			AnalyzerPoolManager analyzerPoolManager = irService.createAnalyzerPoolManager(collectionContext.schema().schemaSetting().getAnalyzerSettingList());
			//find index node
			String indexNodeId = collectionContext.collectionConfig().getIndexNode();
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			Node indexNode = nodeService.getNodeById(indexNodeId);
			
			if(!nodeService.isMyNode(indexNode)){
				//Pass job to index node
				//작업수행하지 않음.
				throw new RuntimeException("Invalid index node collection[" + collectionId + "] node[" + indexNodeId + "]");
			}

			updateIndexingStatusStart();

			/*
			 * Do Document Store!!
			 */
			//////////////////////////////////////////////////////////////////////////////////////////
			SelectedIndexList selectedIndexList = SelectedIndexList.ALL_INDEXING; //TODO 차후 선택적으로 바꾼다.
			boolean isIndexed = false; 
			CollectionIndexBuildIndexer collectionIndexBuildIndexer = new CollectionIndexBuildIndexer(collectionContext, analyzerPoolManager, selectedIndexList);
			indexer = collectionIndexBuildIndexer;
			collectionIndexBuildIndexer.setState(indexingTaskState);
			try {
				indexer.doIndexing();
			} finally {
				if (indexer != null) {
					isIndexed = indexer.close();
				}
			}
			if(!isIndexed && stopRequested){
				//여기서 끝낸다.
				throw new IndexingStopException();
			}
			
			int duration = (int) (System.currentTimeMillis() - startTime);
			
			IndexStatus indexStatus = collectionContext.indexStatus().getFullIndexStatus();
			indexingLogger.info("[{}] Collection Index Build Full Indexing Finished! {} time = {}", collectionId, indexStatus, duration);
			result = new IndexingJobResult(collectionId, indexStatus, duration);
			resultStatus = ResultStatus.SUCCESS;

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
			throw new FastcatSearchException("ERR-00500", throwable, collectionId); // 전체색인실패.
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
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		StreamableCollectionContext streamableCollectionContext = new StreamableCollectionContext(environment);
		streamableCollectionContext.readFrom(input);
		this.collectionContext = streamableCollectionContext.collectionContext();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		StreamableCollectionContext streamableCollectionContext = new StreamableCollectionContext(collectionContext);
		streamableCollectionContext.writeTo(output);
	}

}
