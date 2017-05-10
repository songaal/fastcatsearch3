package org.fastcatsearch.job;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import ch.qos.logback.classic.Level;
import org.fastcatsearch.control.IndexingMutex;
import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.indexing.IndexingJob;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriorityScheduledJobTest {

    Logger logger = LoggerFactory.getLogger(PriorityScheduledJobTest.class);

    @Before
    public void init() {
        String LOG_LEVEL = "DEBUG";
        ((ch.qos.logback.classic.Logger)
                LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME)
        ).setLevel(Level.toLevel(LOG_LEVEL));
    }

	@Test
	public void test() {
		TestJobExecutor jobExecutor = new TestJobExecutor();
		String key = "schedule";
		Date startTime = new Date();
		int period1 = 13;
		int period2 = 2;

		List<ScheduledJobEntry> list = new ArrayList<ScheduledJobEntry>();
		list.add(new ScheduledJobEntry(new TestTaskJob("MUST", 2), startTime, period1, true));
		list.add(new ScheduledJobEntry(new TestTaskJob("00", 2), startTime, period2));
		list.add(new ScheduledJobEntry(new TestTaskJob("01", 2), startTime, period2));
		list.add(new ScheduledJobEntry(new TestTaskJob("02", 1), startTime, period2));
		PriorityScheduledJob scheduledJob = new PriorityScheduledJob(key, list);
		ResultFuture resultFuture = jobExecutor.offer(scheduledJob);
		resultFuture.take();
	}

	@Test
	public void test2() {
		TestJobExecutor jobExecutor = new TestJobExecutor();
		String key = "schedule";
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -5);
		Date startTime1 = cal.getTime();
		cal.add(Calendar.DATE, 3);
		Date startTime2 = cal.getTime();
		System.out.println(startTime1);
		System.out.println(startTime2);
		int period1 = 30;
		int period2 = 10;

		List<ScheduledJobEntry> list = new ArrayList<ScheduledJobEntry>();
		list.add(new ScheduledJobEntry(new TestTaskJob("FULL", 5), startTime1, period1, true));
		list.add(new ScheduledJobEntry(new TestTaskJob("ADD", 2), startTime2, period2));
		PriorityScheduledJob scheduledJob = new PriorityScheduledJob(key, list);
		ResultFuture resultFuture = jobExecutor.offer(scheduledJob);
		resultFuture.take();
	}


    /**
     * 네트워크 에러로 인해 색인노드에서 색인이 끝난 결과를 master 노드에 보내주지 못하면 다음 색인을 시작하지 못한다.
     * 그러므로, period 만큼만 대기하고 해당시간이 지나면 timeout으로 작업이 종료된것으로 간주. 다음 스케쥴 색인 작업은 예정된 시간에 시작되나,
     * 색인 job의 mutext가 존재하므로, 중복 색인작업은 방지할수 있다.
     * */
    @Test
    public void testMultipleRequest() {
        TestJobExecutor jobExecutor = new TestJobExecutor();
        String key = "full-indexing";
        Date startTime = new Date();
        int period1 = 2;
        List<ScheduledJobEntry> list = new ArrayList<ScheduledJobEntry>();
        list.add(new ScheduledJobEntry(new TestFullIndexingJob("col1", 10), startTime, period1, true));
        PriorityScheduledJob scheduledJob = new PriorityScheduledJob(key, list);
        ResultFuture resultFuture = jobExecutor.offer(scheduledJob);
        resultFuture.take();
    }

}

class TestFullIndexingJob extends IndexingJob {

    String name;
    int executeTime;
    public TestFullIndexingJob(String collectionId, int executeTime){
        setArgs(collectionId);
        this.name = collectionId;
        this.executeTime = executeTime;
    }
    @Override
    public JobResult doRun() throws FastcatSearchException {
        System.out.println(name + "] Indexing start!");

        try {
            for (int i = 0; i < executeTime; i++) {
                Thread.sleep(1000L);
                System.out.println("indexing..." + i);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(name + "] Indexing Finish!");
        return new JobResult(true);
    }

}

class TestTaskJob extends Job {
	String name;
	int executeTime;
	public TestTaskJob(String name, int executeTime){
		setArgs(name);
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
    private IndexingMutex indexingMutex = new IndexingMutex();

    Logger logger = LoggerFactory.getLogger(PriorityScheduledJobTest.class);

	@Override
	public ResultFuture offer(Job job) {
        job.setJobExecutor(this);

        if (job instanceof IndexingJob) {
            if (indexingMutex.isLocked((IndexingJob) job)) {
                logger.info("The collection [{}] has already started an indexing job.", job.getStringArgs());
                return new ResultFuture();
            }
        }

        long myJobId = jobIdIncrement.getAndIncrement();
//		logger.debug("### OFFER Job-{} : {}", myJobId, job.getClass().getSimpleName());

        if (job instanceof IndexingJob) {
            indexingMutex.access(myJobId, (IndexingJob) job);
        }
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

        //색인작업 락을 풀어준다.
        if (job instanceof IndexingJob) {
            indexingMutex.release(jobId);
        }

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