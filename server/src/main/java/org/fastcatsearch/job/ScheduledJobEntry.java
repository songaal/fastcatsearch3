package org.fastcatsearch.job;

import java.util.Date;

/**
 * 스케쥴 작업을 할 job과 스케줄 시간.
 * */
public class ScheduledJobEntry {
	private Job job;
	private Date startTime;
	private int periodInSecond;
	private boolean isExecuteGuarantee; // 실행이 보장되는지 여부. 함께 수행되는 job때문에 실행시작이 늦어지더라도 차후에 반드시 수행된다. 단 동일 job이 연속으로 계속 실행되지는 않는다.
	private ScheduledJobExecuteInfo executeInfo;

	public ScheduledJobEntry(Job job, Date startTime, int periodInSecond) {
		this(job, startTime, periodInSecond, false);
	}

	public ScheduledJobEntry(Job job, Date startTime, int periodInSecond, boolean isExecuteGuarantee) {
		this.job = job;
		job.setScheduled(true);
		this.startTime = startTime;
		this.periodInSecond = periodInSecond;
		this.isExecuteGuarantee = isExecuteGuarantee;
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

	public boolean isExecuteGuarantee() {
		return isExecuteGuarantee;
	}

	public ScheduledJobExecuteInfo executeInfo() {
		return executeInfo;
	}

	@Override
	public String toString() {
		return job.getClass().getSimpleName() + " : " + job.getArgs() + " : st[" + startTime + "] : per[" + periodInSecond + "] : gr[" + isExecuteGuarantee + "] : " + executeInfo;
	}

	public static class ScheduledJobExecuteInfo {
		private Date lastExecuteTime;
		private long executeCount;

		public long executeCount() {
			return executeCount;
		}

		public Date lastExecuteTime() {
			return lastExecuteTime;
		}

		@Override
		public String toString() {
			return "ExecuteCount[" + executeCount + "] LastTime[" + lastExecuteTime + "]";
		}

		public ScheduledJobExecuteInfo incrementExecution() {
			executeCount++;
			lastExecuteTime = new Date();
			return this;
		}
	}
}
