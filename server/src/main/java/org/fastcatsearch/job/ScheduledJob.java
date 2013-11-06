package org.fastcatsearch.job;

import java.util.Date;

import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.util.DynamicClassLoader;

public class ScheduledJob extends Job {
	private static final long serialVersionUID = -7810770586290536202L;

	private Job actualJob;
	private Date startTime;
	private int periodInSecond; // 66년까지 표현가능.
	private boolean isCanceled;
	private int executeCount;
	private Date lastExecuteTime;

	public ScheduledJob(Job job, Date startTime, int periodInSecond) {
		this.actualJob = job;
		this.actualJob.setScheduled(true);
		this.startTime = startTime;
		this.periodInSecond = periodInSecond;
	}

	public ScheduledJob(String jobClassName, String args) {
		actualJob = DynamicClassLoader.loadObject(jobClassName, Job.class);
		String[] arglist = args.split("\t");
		actualJob.setArgs(arglist);
		actualJob.setScheduled(true); // scheduled job.
	}

	public void cancel() {
		isCanceled = true;
		Thread.currentThread().interrupt();
	}

	public Job actualJob() {
		return actualJob;
	}

	public Date startTime() {
		return startTime;
	}

	public int periodInSecond() {
		return periodInSecond;
	}

	public int executeCount() {
		return executeCount;
	}

	public Date lastExecuteTime() {
		return lastExecuteTime;
	}

	public boolean isCanceled() {
		return isCanceled;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		if (isCanceled) {
			return new JobResult();
		}
		try {
			Thread.sleep(getTimeToWaitInMillisecond());
		} catch (InterruptedException e) {
			// if cancel method is called.
			logger.info("[{}] {}({}) is canceled.", getClass().getSimpleName(), actualJob.getClass().getSimpleName(), actualJob.getArgs());
			return new JobResult();
		}

		try {
			ResultFuture resultFuture = jobExecutor.offer(actualJob);
			Object result = null;
			if (resultFuture == null) {
				// ignore
				logger.debug("Scheduled job {} is ignored.", actualJob);
			} else {
				result = resultFuture.take();
				logger.debug("Schedule Job Result = {}", result);
			}
			return new JobResult();
		} finally {
			executeCount++;
			lastExecuteTime = new Date();

			if (!isCanceled) {
				jobExecutor.offer(this);
			}
		}
	}

	private long getTimeToWaitInMillisecond() {
		if (startTime.getTime() > System.currentTimeMillis()) {
			return startTime.getTime() - System.currentTimeMillis();
		} else {
			long newFirstTime = startTime.getTime();
			while (newFirstTime < System.currentTimeMillis()) {
				newFirstTime += (periodInSecond * 1000L); // increase by period
			}
			// logger.debug("newFirstTime = {} period={}", new
			// Date(newFirstTime), periodInSecond);
			return newFirstTime - System.currentTimeMillis();
		}
	}

}
