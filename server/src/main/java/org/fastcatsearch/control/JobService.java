/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.control;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

import org.fastcatsearch.common.ThreadPoolFactory;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.PriorityScheduledJob;
import org.fastcatsearch.job.ScheduledJob;
import org.fastcatsearch.job.ScheduledJobEntry;
import org.fastcatsearch.job.indexing.IndexingJob;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author sangwook.song
 * 
 */

public class JobService extends AbstractService implements JobExecutor {
	private static Logger logger = LoggerFactory.getLogger(JobService.class);
	private static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

	private BlockingQueue<Job> jobQueue;
	private Map<Long, ResultFuture> resultFutureMap;
	private BlockingQueue<Job> sequencialJobQueue;
	private Map<Long, Job> runningJobList;
	private AtomicLong jobIdIncrement;

	private ThreadPoolExecutor jobExecutor;

	private JobConsumer worker;
	private SequencialJobWorker sequencialJobWorker;
	private IndexingMutex indexingMutex;
//	private boolean useJobScheduler;
	private int executorMaxPoolSize;
	private static JobService instance;
	private ConcurrentHashMap<String, ScheduledJob> scheduleMap; 

	public void asSingleton() {
		instance = this;
	}

	public static JobService getInstance() {
		return instance;
	}

	public JobService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	protected boolean doStart() throws FastcatSearchException {
		jobIdIncrement = new AtomicLong();
		resultFutureMap = new ConcurrentHashMap<Long, ResultFuture>();
		runningJobList = new ConcurrentHashMap<Long, Job>();
		jobQueue = new LinkedBlockingQueue<Job>();
		sequencialJobQueue = new LinkedBlockingQueue<Job>();
		indexingMutex = new IndexingMutex();

//		executorMaxPoolSize = settings.getInt("pool.max");

		// int indexJobMaxSize = 100;
		// int searchJobMaxSize = 100;
		// int otherJobMaxSize = 100;

//		jobExecutor = ThreadPoolFactory.newCachedThreadPool("JobService.jobExecutor", executorMaxPoolSize);
		jobExecutor = ThreadPoolFactory.newUnlimitedCachedDaemonThreadPool("JobService.jobExecutor");

		worker = new JobConsumer();
		worker.start();
		sequencialJobWorker = new SequencialJobWorker();
		sequencialJobWorker.start();

		scheduleMap = new ConcurrentHashMap<String, ScheduledJob>();

		return true;
	}

	protected boolean doStop() {
		logger.debug("{} stop requested.", getClass().getName());
		worker.interrupt();
		sequencialJobWorker.interrupt();
		resultFutureMap.clear();
		jobQueue.clear();
		sequencialJobQueue.clear();
		runningJobList.clear();
		jobExecutor.shutdownNow();
		
		for(ScheduledJob job : scheduleMap.values()){
			job.cancel();
		}
		scheduleMap.clear();
		
		return true;
	}

	protected boolean doClose() {
		return true;
	}

	public int runningJobSize() {
		return runningJobList.size();
	}

	public int inQueueJobSize() {
		return jobQueue.size();
	}

	public Collection<Job> getRunningJobs() {
		return runningJobList.values();
	}
	
	public Job findRunningJob(Job findJob){
		String findJobKey = getJobKey(findJob);
		for(Job job : runningJobList.values()){
			if(getJobKey(job).equals(findJobKey)){
				return job;
			}
		}
		return null;
	}
	
	public Job findRunningJob(Class<? extends Job> clazz, Object args){
		for(Job job : runningJobList.values()){
			if(clazz.isAssignableFrom(job.getClass())){
				if(job.getArgs().equals(args)){
					return job;
				}
			}
		}
		return null;
	}
	
	public Collection<ScheduledJob> getScheduledJobs() {
		return scheduleMap.values();
	}

	public Collection<String> getIndexingList() {
		return indexingMutex.getIndexingList();
	}

	public ThreadPoolExecutor getJobExecutor() {
		return jobExecutor;
	}

	/**
	 * 순차적인 작업을 실행할때 호출한다. 도착한 순서대로 앞의 작업이 모두 끝나야 다음작업이 실행된다.
	 * */
	public ResultFuture offerSequential(Job job) {
		job.setEnvironment(environment);
		job.setJobExecutor(this);

		if (job instanceof IndexingJob) {
			// 색인작업은 execute로 실행할수 없다.
			return new ResultFuture();
		}
		long myJobId = jobIdIncrement.getAndIncrement();
		ResultFuture resultFuture = new ResultFuture(myJobId, resultFutureMap, job);
		resultFutureMap.put(myJobId, resultFuture);
		job.setId(myJobId);
		sequencialJobQueue.offer(job);
		return resultFuture;
	}

	public ResultFuture offer(Job job) {
		job.setEnvironment(environment);
		job.setJobExecutor(this);

		if (job instanceof IndexingJob) {
			if (indexingMutex.isLocked((IndexingJob) job)) {
				indexingLogger.info("The collection [{}] has already started an indexing job.", job.getStringArgs());
				return new ResultFuture();
			}
		}

		long myJobId = jobIdIncrement.getAndIncrement();
//		logger.debug("### OFFER Job-{} : {}", myJobId, job.getClass().getSimpleName());

		if (job instanceof IndexingJob) {
			indexingMutex.access(myJobId, (IndexingJob) job);
		}

		if (job.isNoResult()) {
			job.setId(myJobId);
			jobQueue.offer(job);
			return null;
		} else {
			ResultFuture resultFuture = new ResultFuture(myJobId, resultFutureMap, job);
			resultFutureMap.put(myJobId, resultFuture);
			job.setId(myJobId);
			jobQueue.offer(job);
			return resultFuture;
		}
	}

	public void result(Job job, Object result, boolean isSuccess) {
		long jobId = job.getId();
		runningJobList.remove(jobId);

		//색인작업 락을 풀어준다.
		if (job instanceof IndexingJob) {
			indexingMutex.release(jobId);
		}

		//noresult 작업의 경우 resultFuture 가 null이므로 결과 셋팅을 하지 않게 된다.
		ResultFuture resultFuture = resultFutureMap.remove(jobId);
		if (resultFuture != null) {
			resultFuture.put(result, isSuccess);
		} else {
			// 시간초과로 ResultFutuer.poll에서 미리제거된 경우.
			// 혹은 처음부터 결과가 필요없어서 map에 안넣었을 경우.
			// logger.debug("result arrived but future object is already removed due to timeout. result={}", result);
		}

	}
	public void schedule(ScheduledJob scheduledJob){
		schedule(scheduledJob, false);
	}
	public void schedule(ScheduledJob scheduledJob, boolean forceUpdate){
		String key = scheduledJob.key();
		ScheduledJob oldJob = scheduleMap.get(key);
		if(oldJob == null || forceUpdate){
			ScheduledJob oldScheduledJob = scheduleMap.put(key, scheduledJob);
			if(oldScheduledJob != null){
				logger.info("Cancel old schdule {}", oldScheduledJob);
				oldScheduledJob.cancel();
			}
			logger.info("# Scheduled Job registerd >> {}", scheduledJob.toString());
			offer(scheduledJob);
		}else{
			logger.error("{} is already scheduled > {}", key, key);
		}
		showScheduledJob();
	}
	
	public void schedule(Job job, Date startTime, int periodInSecond){
		schedule(job, startTime, periodInSecond, false);
	}
	
	public void schedule(Job job, Date startTime, int periodInSecond, boolean forceUpdate){
		if(periodInSecond < 0){
			logger.error("Schedule must be 0 or positive number. periodInSecond = {}", periodInSecond);
			return;
		}
		
		String scheduledJobKey = getJobKey(job);
		ScheduledJobEntry scheduledJobEntry = new org.fastcatsearch.job.ScheduledJobEntry(job, startTime, periodInSecond);
		PriorityScheduledJob scheduledJob = new PriorityScheduledJob(scheduledJobKey, scheduledJobEntry);
		schedule(scheduledJob, forceUpdate);
	}
	
	private void showScheduledJob(){
		logger.debug("----------------------");
		for(Entry<String, ScheduledJob> entry : scheduleMap.entrySet()){
			logger.debug(">>> {} : {}", entry.getKey(), entry.getValue());
		}
		logger.debug("----------------------");
	}
	
	public void cancelSchedule(String key){
		ScheduledJob scheduledJob = scheduleMap.remove(key);
		if(scheduledJob != null){
			logger.debug("## cancel schedule {} : {}", key, scheduledJob);
			scheduledJob.cancel();
		}
		showScheduledJob();
	}
	
	private String getJobKey(Job job){
		if(job != null){
			if(job.getArgs() != null){
				return job.getClass().getName() + "\t" + job.getArgs();
			}
			return job.getClass().getName();
		}
		return null;
	}
	
	/*
	 * 이 쓰레드는 절대로 죽어서는 아니되오.
	 */
	class JobConsumer extends Thread {
		// 2013-4-5 exception발생시 worker가 죽어서 더이상 작업할당을 못하는 상황발생.
		// InterruptedException을 제외한 모든 throwable을 catch하도록 수정.

		public JobConsumer() {
			super("JobConsumerThread");
		}

		public void run() {
			while (!Thread.interrupted()) {
				Job job = null;
				try {
					job = jobQueue.take();
					runningJobList.put(job.getId(), job);
					jobExecutor.execute(job);
				} catch (InterruptedException e) {
					logger.debug(this.getClass().getName() + " is interrupted.");
				} catch (RejectedExecutionException e) {
					// jobExecutor rejecthandler가 abortpolicy의 경우
					// RejectedExecutionException을 던지게 되어있다.
					logger.error("처리허용량을 초과하여 작업이 거부되었습니다. max.pool = {}, job={}", executorMaxPoolSize, job);
					result(job, new ExecutorMaxCapacityExceedException("처리허용량을 초과하여 작업이 거부되었습니다. max.pool =" + executorMaxPoolSize), false);

				} catch (Throwable e) {
					logger.error("", e);
				}
			}

		}
	}

	/*
	 * 이 쓰레드는 절대로 죽어서는 아니되오.
	 */
	class SequencialJobWorker extends Thread {
		public SequencialJobWorker() {
			super("SequencialJobWorker");
		}

		public void run() {
			while (!Thread.interrupted()) {
				Job job = null;
				try {
					job = sequencialJobQueue.take();
					// 이 쓰레드에서 바로 실행해준다. 순서보장됨.
					job.run();
				} catch (InterruptedException e) {
					logger.debug(this.getClass().getName() + " is interrupted.");
				} catch (Throwable e) {
					logger.error("", e);
				}
			}

		}
	}

}
