package org.fastcatsearch.job;

import java.util.Date;

/**
 * 스케쥴 작업을 할 job과 스케줄 시간.
 * */
public class ScheduledJobEntry {
	private Job job;
	private Date startTime;
	private int periodInSecond;
	private ScheduledJobExecuteInfo executeInfo;
	
	
	public ScheduledJobEntry(Job job, Date startTime, int periodInSecond){
		this.job = job;
		this.startTime = startTime;
		this.periodInSecond = periodInSecond;
		this.executeInfo = new ScheduledJobExecuteInfo();
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public int getPeriodInSecond() {
		return periodInSecond;
	}

	public void setPeriodInSecond(int periodInSecond) {
		this.periodInSecond = periodInSecond;
	}
	
	public ScheduledJobExecuteInfo executeInfo(){
		return executeInfo;
	}
	
	@Override
	public String toString(){
		return job.getClass().getSimpleName() +" : " + job.getArgs() + " : " + executeInfo;
	}
	
	
	public static class ScheduledJobExecuteInfo {
		private Date lastExecuteTime;
		private long executeCount;
		
		public long executeCount(){
			return executeCount;
		}
		
		public Date lastExecuteTime(){
			return lastExecuteTime;
		}
		
		@Override
		public String toString(){
			return "ExecuteCount["+executeCount+"] LastTime["+lastExecuteTime+"]";
		}
		
		public ScheduledJobExecuteInfo incrementExecution(){
			executeCount++;
			lastExecuteTime = new Date();
			return this;
		}
	}
}
