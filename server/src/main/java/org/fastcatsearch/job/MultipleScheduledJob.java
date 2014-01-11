package org.fastcatsearch.job;

import java.util.Date;
import java.util.List;

import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;

/**
 * 입력된 여러 스케쥴은 한번에 하나씩만 수행된다. 스케쥴 시간이 겹치는 경우, 앞쪽의 작업이 먼저 수행된다.
 * */
public class MultipleScheduledJob extends ScheduledJob {

	private static final long serialVersionUID = -8978933991846840288L;
	private List<ScheduledJobEntry> entryList;
	protected boolean isCanceled;

	public MultipleScheduledJob(String key, List<ScheduledJobEntry> entryList) {
		super(key);
		this.entryList = entryList;
	}
	@Override
	public void cancel() {
		isCanceled = true;
		synchronized (this) {
			this.notify();
		}
	}
	@Override
	public boolean isCanceled() {
		return isCanceled;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		while (!isCanceled) {
			try {
				ScheduledJobEntry entry = getHighPriorityEntry();
				Job actualJob = entry.getJob();
				try {
					long timeToWait = getTimeToWaitInMillisecond(entry);
					logger.info("Next {} indexing will run {} at {} after waiting {}ms", key(), actualJob.getClass().getSimpleName(), entry.getStartTime(), timeToWait);
					if (timeToWait > 0) {
						synchronized (this) {
							wait(timeToWait);
						}
					}
				} catch (InterruptedException e) {
					// if cancel method is called.
					logger.info("[{}] is canceled >> {}", getClass().getSimpleName(), entryList);
					return new JobResult();
				}

				if (isCanceled) {
					logger.info("[{}] is canceled >> {}", getClass().getSimpleName(), entryList);
					return new JobResult();
				}

				try {
					logger.debug("##### Scheduled Job offer {} {}", actualJob, entry);
					ResultFuture resultFuture = jobExecutor.offer(actualJob);
					Object result = null;
					if (resultFuture == null) {
						// ignore
						logger.debug("Scheduled job {} is ignored.", actualJob);
					} else {
						result = resultFuture.take();
						entry.executeInfo().incrementExecution();
						logger.debug("Scheduled Job Finished. {} > {}, execution[{}]", actualJob, result, entry.executeInfo());
					}
					return new JobResult();
				} finally {
					
					
					if (!isCanceled) {
						jobExecutor.offer(this);
					}
				}
				
			} catch (Throwable t) {
				//죽지마.
				logger.error("", t);
			}
		}

		if (isCanceled) {
			logger.info("[{}] is canceled >> {}", getClass().getSimpleName(), entryList);
		}
		return new JobResult();
	}

	private ScheduledJobEntry getHighPriorityEntry() {

		updateStartTimeByNow();

		ScheduledJobEntry highEntry = entryList.get(0);
		for (ScheduledJobEntry entry2 : entryList) {
			if (highEntry.getStartTime().compareTo(entry2.getStartTime()) > 0) {
				highEntry = entry2;
			}
		}

		return highEntry;
	}

	// 현시간기준으로 다음 시작시간으로 업데이트.
	protected void updateStartTimeByNow() {
		for (ScheduledJobEntry entry : entryList) {
			Date startTime = entry.getStartTime();
			int periodInSecond = entry.getPeriodInSecond();
			periodInSecond *= 1000L;

			long nextStartTime = startTime.getTime();
			long now = System.currentTimeMillis();

			if (nextStartTime < now) {
				// 현 시간보다 커질때까지 더한다.
				while (nextStartTime < now) {
					nextStartTime += periodInSecond;// increase by period
				}
				entry.setStartTime(new Date(nextStartTime));
			} else {
				// 현시간보다 크면 그대로 둔다.
			}
		}
	}

	protected long getTimeToWaitInMillisecond(ScheduledJobEntry entry) {
		long nextStartTime = entry.getStartTime().getTime();
		long now = System.currentTimeMillis();
		if (nextStartTime > now) {
			return nextStartTime - now;
		} else {
			return 0; // 즉시 시작.
		}
	}

}
