package org.fastcatsearch.control;

import org.fastcatsearch.job.Job;

public interface JobExecutor {
	
	public ResultFuture offer(Job job);
		
	public void result(Job job, Object result, boolean isSuccess);
	
	public int runningJobSize();
	public int inQueueJobSize();
}
