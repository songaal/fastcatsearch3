package org.fastcatsearch.job;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 입력된 여러 스케쥴은 한번에 하나씩만 수행된다. 스케쥴 시간이 겹치는 경우, guarantee job이 우선하고, 
 * guarantee job끼리 시간이 겹치는 경우는 어느것이 먼저 실행될지 알수없으나, 모두 실행되는 것이 보장된다. 
 * */
public class PriorityScheduledJob extends ScheduledJob {

	private static final long serialVersionUID = -8978933991846840288L;
	private Queue<ScheduledJobEntry> priorityJobQueue;
	protected boolean isCanceled;

	private final static ScheduledJobEntryComparator comparator = new ScheduledJobEntryComparator();

	public PriorityScheduledJob(String key, ScheduledJobEntry scheduledJobEntry) {
		super(key);
		this.priorityJobQueue = new PriorityQueue<ScheduledJobEntry>(3, comparator);
		priorityJobQueue.add(scheduledJobEntry);
	}

	public PriorityScheduledJob(String key, List<ScheduledJobEntry> list) {
		super(key);
		this.priorityJobQueue = new PriorityQueue<ScheduledJobEntry>(3, comparator);
		priorityJobQueue.addAll(list);
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
		//처음에 시작시간을 모두 업데이트 해준다.
		Iterator<ScheduledJobEntry> iterator = priorityJobQueue.iterator();
		while(iterator.hasNext()){
			ScheduledJobEntry e = iterator.next();
			updateStartTimeByNow(e);
		}
		while (!isCanceled) {
			try {
				ScheduledJobEntry entry = priorityJobQueue.poll();
				Job actualJob = entry.getJob();
				long timeToWait = getTimeToWaitInMillisecond(entry);
				if (timeToWait < 0) {
					// 이미 지났을 경우.
					if (entry.isExecuteGuarantee()) {
						// 1) guarantee job: 바로실행한다.
						timeToWait = 0;
					} else {
						// 2) normal job: next start time을 계산하여 다시 Q에 집어넣는다.
//						logger.debug("지난 작업 스킵 : {}", entry);
						updateStartTimeByNow(entry);
						priorityJobQueue.offer(entry);
						continue;
					}
				}

				logger.info("Next {} indexing will run {} at {} after waiting {}ms", key(), actualJob.getClass().getSimpleName(), entry.getStartTime(), timeToWait);
				if(timeToWait > 0){
					synchronized (this) {
						wait(timeToWait);
					}
				}
				
				if (isCanceled) {
					break;
				}

				try {
					logger.debug("##### Scheduled Job offer {} : {}", actualJob, entry);
					ResultFuture resultFuture = jobExecutor.offer(actualJob);
					Object result = null;
					if (resultFuture == null) {
						// ignore
						logger.debug("Scheduled job {} is ignored.", actualJob);
					} else {
						result = resultFuture.take();
						logger.debug("Scheduled Job Finished. {} > {}, execution[{}]", actualJob, result, entry.executeInfo());
					}
				} finally {
					// 실행한 job에 대해서는 반드시 다음 시간에 실행되도록 update time후 offer되도록 한다.
					entry.executeInfo().incrementExecution();
					updateStartTimeByNow(entry);
					priorityJobQueue.offer(entry);
				}

			} catch (InterruptedException e) {
				// InterruptedException 은 thread를 끝내게 한다.
				logger.info("[{}] is interrupted!", getClass().getSimpleName());
				break;
			} catch (Throwable t) {
				// 죽지마.
				logger.error("", t);
			}
		}

		if (isCanceled) {
			logger.info("[{}] is canceled >> {}", getClass().getSimpleName(), priorityJobQueue);
		}
		return new JobResult();
	}

	// 현시간기준으로 다음 시작시간으로 업데이트.
	protected void updateStartTimeByNow(ScheduledJobEntry entry) {
		Date startTime = entry.getStartTime();
		int periodInSecond = entry.getPeriodInSecond();
		periodInSecond *= 1000L;

		long nextStartTime = startTime.getTime();
		long now = System.currentTimeMillis();

		if (nextStartTime < now) {
			// 현 시간보다 커질때까지 더한다.
			if(periodInSecond > 0){
				//주기가 0이 아닐때만 더해서 다음 시간을 구한다.
				while (nextStartTime < now) {
					nextStartTime += periodInSecond;// increase by period
				}
			}else{
				//주기가 0 이라면 바로시작한다. delay time = 1초.
				nextStartTime = now + 1000;
			}
			entry.setStartTime(new Date(nextStartTime));
		} else {
			// 현시간보다 크면 그대로 둔다.
		}
	}

	protected long getTimeToWaitInMillisecond(ScheduledJobEntry entry) {
		long nextStartTime = entry.getStartTime().getTime();
		return nextStartTime - System.currentTimeMillis();
	}

}

class ScheduledJobEntryComparator implements Comparator<ScheduledJobEntry> {
	protected static Logger logger = LoggerFactory.getLogger(ScheduledJobEntryComparator.class);

	@Override
	public int compare(ScheduledJobEntry o1, ScheduledJobEntry o2) {
//		logger.debug("1>>{}", o1);
//		logger.debug("2>>{}", o2);
		int c = o1.getStartTime().compareTo(o2.getStartTime());
		if(c == 0){
			if(o1.isExecuteGuarantee()){
				return -1;
			}else if(o2.isExecuteGuarantee()){
				return 1;
			}
			
			return 0;
		}else{
			return c;
		}
	}

}
