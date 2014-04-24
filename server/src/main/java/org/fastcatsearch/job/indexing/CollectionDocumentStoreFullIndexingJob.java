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
import org.fastcatsearch.ir.CollectionFullDocumentStorer;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.CollectionIndexStatus.IndexStatus;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job.JobResult;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableCollectionContext;
import org.fastcatsearch.transport.vo.StreamableThrowable;

/**
 * 전체색인용 문서만를 저장한다. 색인은 생성하지 않는다.
 * 색인은 차후 build index시 생성하도록 한다.
 * @see CollectionIndexBuildFullIndexingJob
 * 
 * index node가 아닌 노드에 전달되면 색인을 수행하지 않는다.
 *  
 * */
public class CollectionDocumentStoreFullIndexingJob extends IndexingJob {

	private static final long serialVersionUID = -4291415269961866851L;
	private CollectionContext collectionContext; 
	
	public CollectionDocumentStoreFullIndexingJob(){
	}
	
	public CollectionDocumentStoreFullIndexingJob(CollectionContext collectionContext){
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
		
		prepare(IndexingType.FULL_DOCUMENT_STORE, "ALL");
		
		
		Throwable throwable = null;
		ResultStatus resultStatus = ResultStatus.RUNNING;
		Object result = null;
		long startTime = System.currentTimeMillis();
		try {
			//find index node
			String indexNodeId = collectionContext.collectionConfig().getIndexNode();
			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			Node indexNode = nodeService.getNodeById(indexNodeId);
			
			if(!nodeService.isMyNode(indexNode)){
				//Pass job to index node
				//작업수행하지 않음.
				throw new RuntimeException("Invalid index node collection[" + collectionId + "] node[" + indexNodeId + "]");
			}

			if(!updateIndexingStatusStart()) {
				resultStatus = ResultStatus.CANCEL;
				return new JobResult();
			}

			/*
			 * Do Document Store!!
			 */
			//////////////////////////////////////////////////////////////////////////////////////////
			
			boolean isIndexed = false; 
			CollectionFullDocumentStorer collectionFullDocumentStorer = new CollectionFullDocumentStorer(collectionContext);
			indexer = collectionFullDocumentStorer;
			collectionFullDocumentStorer.setTaskState(indexingTaskState);
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
			indexingLogger.info("[{}] Collection Doucument Store Full Indexing Finished! {} time = {}", collectionId, indexStatus, duration);
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
