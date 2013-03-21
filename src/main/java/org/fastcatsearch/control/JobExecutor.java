package org.fastcatsearch.control;

import org.fastcatsearch.job.Job;

public interface JobExecutor {
	
	public JobResult offer(Job job);
		
	public void result(long jobId, Job job, Object result, boolean isSuccess, long st, long et);
	
}
