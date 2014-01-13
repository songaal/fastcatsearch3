package org.fastcatsearch.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.junit.Test;

public class PriorityScheduledJobTest {

	@Test
	public void test() {
		TestJobExecutor jobExecutor = new TestJobExecutor();
		String key = "schedule";
		Date startTime = new Date();
		int period1 = 13;
		int period2 = 2;
		
		List<ScheduledJobEntry> list = new ArrayList<ScheduledJobEntry>();
		list.add(new ScheduledJobEntry(new TetsJob("MUST", 2), startTime, period1, true));
		list.add(new ScheduledJobEntry(new TetsJob("00", 2), startTime, period2));
		list.add(new ScheduledJobEntry(new TetsJob("01", 2), startTime, period2));
		list.add(new ScheduledJobEntry(new TetsJob("02", 1), startTime, period2));
		PriorityScheduledJob scheduledJob = new PriorityScheduledJob(key, list);
		ResultFuture resultFuture = jobExecutor.offer(scheduledJob);
		resultFuture.take();
	}

}

class TetsJob extends Job {
	String name;
	int executeTime;
	public TetsJob(String name, int executeTime){
		this.name = name;
		this.executeTime = executeTime * 1000;
	}
	@Override
	public JobResult doRun() throws FastcatSearchException {
		System.out.println(name + "] Job start!");

		try {
			Thread.sleep(executeTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(name + "] Job Finish!");
		return new JobResult(true);
	}

}

class TestJobExecutor implements JobExecutor {

	AtomicLong jobIdIncrement = new AtomicLong();
	Map<Long, ResultFuture> resultFutureMap = new ConcurrentHashMap<Long, ResultFuture>();

	@Override
	public ResultFuture offer(Job job) {

		long myJobId = jobIdIncrement.getAndIncrement();
		ResultFuture resultFuture = new ResultFuture(myJobId, resultFutureMap);
		resultFutureMap.put(myJobId, resultFuture);
		job.setJobExecutor(this);
		job.setId(myJobId);
		new Thread(job).start();
		return resultFuture;
	}

	@Override
	public void result(Job job, Object result, boolean isSuccess) {
		long jobId = job.getId();
		ResultFuture resultFuture = resultFutureMap.remove(jobId);

		if (resultFuture != null) {
			resultFuture.put(result, isSuccess);
		}
	}

	@Override
	public int runningJobSize() {
		return 0;
	}

	@Override
	public int inQueueJobSize() {
		return 0;
	}

}