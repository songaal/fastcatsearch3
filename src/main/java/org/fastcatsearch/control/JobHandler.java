package org.fastcatsearch.control;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.job.FullIndexJob;
import org.fastcatsearch.job.IncIndexJob;
import org.fastcatsearch.job.IndexingJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.Job.JobResult;
import org.fastcatsearch.job.action.FullIndexRequest;
import org.fastcatsearch.job.notification.IndexingFinishNotification;
import org.fastcatsearch.job.notification.IndexingStartNotification;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class JobHandler {

	private NodeService nodeService;
	
	public JobHandler(NodeService service) {
		this.nodeService = service;
		
	}
	
	public void handleError(Job job, Throwable e) {
		
		if(job instanceof IndexingJob){
			String collection = job.getStringArgs(0);
			String indexingType = "-";
			if (job instanceof FullIndexJob || job instanceof FullIndexRequest){
				indexingType = IndexingResult.TYPE_FULL_INDEXING;
			}else if (job instanceof IncIndexJob){
				indexingType = IndexingResult.TYPE_INC_INDEXING;
			}
			nodeService.sendRequestToMaster(new IndexingFinishNotification(collection, indexingType, false, job.startTime(), job.endTime(), new StreamableThrowable(e)));
		}
	}

	public void handleStart(Job job) {
		
		if(job instanceof IndexingJob){
			String collection = job.getStringArgs(0);
			String indexingType = "-";
			if (job instanceof FullIndexJob || job instanceof FullIndexRequest){
				indexingType = IndexingResult.TYPE_FULL_INDEXING;
			}else if (job instanceof IncIndexJob){
				indexingType = IndexingResult.TYPE_INC_INDEXING;
			}
	
			//결과는 무시한다.
			nodeService.sendRequestToMaster(new IndexingStartNotification(collection, indexingType, job.startTime(), job.isScheduled()));
		}
	}

	public void handleFinish(Job job, JobResult jobResult) {
		if(job instanceof IndexingJob){
			String collection = job.getStringArgs(0);
			String indexingType = "-";
			if (job instanceof FullIndexJob || job instanceof FullIndexRequest){
				indexingType = IndexingResult.TYPE_FULL_INDEXING;
			}else if (job instanceof IncIndexJob){
				indexingType = IndexingResult.TYPE_INC_INDEXING;
			}
			boolean isSuccess = jobResult.isSuccess();
			Streamable result = (Streamable) jobResult.result();
			//결과는 무시한다.
			nodeService.sendRequestToMaster(new IndexingFinishNotification(collection, indexingType, isSuccess, job.startTime(), job.endTime(), result));
			
		}
		
	}

}
