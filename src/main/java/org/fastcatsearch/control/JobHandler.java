package org.fastcatsearch.control;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.job.FullIndexJob;
import org.fastcatsearch.job.IncIndexJob;
import org.fastcatsearch.job.IndexingJob;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.Job.JobResult;
import org.fastcatsearch.job.action.FullIndexRequest;
import org.fastcatsearch.job.notification.IndexingFinishNotification;
import org.fastcatsearch.job.notification.IndexingStartNotification;

public class JobHandler {

	private NodeService nodeService;
	
	public JobHandler(NodeService service) {
		this.nodeService = service;
		
	}
	
	public void handleError(Job job, Throwable e) {
		
	}

	public void handleStart(Job job) {
		
		if(job instanceof IndexingJob){
			String collection = job.getStringArgs(0);
			String indexingType = "-";
			if (job instanceof FullIndexJob || job instanceof FullIndexRequest){
				indexingType = "F";
			}else if (job instanceof IncIndexJob){
				indexingType = "I";
			}
	
			//결과는 무시한다.
			nodeService.sendRequestToMaster(new IndexingStartNotification(collection, indexingType, job.startTime()));
		}
	}

	public void handleFinish(Job job, JobResult jobResult) {
		if(job instanceof IndexingJob){
			String collection = job.getStringArgs(0);
			String indexingType = "-";
			if (job instanceof FullIndexJob || job instanceof FullIndexRequest){
				indexingType = "F";
			}else if (job instanceof IncIndexJob){
				indexingType = "I";
			}
			boolean isSuccess = jobResult.isSuccess();
			Streamable result = (Streamable) jobResult.result();
			//결과는 무시한다.
			nodeService.sendRequestToMaster(new IndexingFinishNotification(collection, indexingType, isSuccess, job.startTime(), job.endTime(), result));
			
		}
		
	}

}
