package org.fastcatsearch.ir.index.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexWriteTaskPool {
	private static Logger logger = LoggerFactory.getLogger(IndexWriteTaskPool.class);
			
	private BlockingQueue<IndexWriteTask> taskQueue;
	
	public IndexWriteTaskPool(ThreadPoolExecutor jobExecutor, int poolSize) {
		taskQueue = new LinkedBlockingQueue<IndexWriteTask>();
		for(int i = 0; i < poolSize; i++) {
			IndexWriteTaskWorker worker = new IndexWriteTaskWorker(taskQueue);
			jobExecutor.execute(worker);
		}
	}
	
	public void start() {
		
	}
	public void finish() {
		
	}
	public BlockingQueue<IndexWriteTask> taskQueue() {
		return taskQueue;
	}
	
}

class IndexWriteTaskWorker implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(IndexWriteTaskWorker.class);
	
	private BlockingQueue<IndexWriteTask> taskQueue;
	
	public IndexWriteTaskWorker(BlockingQueue<IndexWriteTask> taskQueue) {
		this.taskQueue = taskQueue;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
//				logger.debug(" taskQueue.size > {}", taskQueue.size());
				IndexWriteTask task = taskQueue.take();
//				logger.debug(" take task > {}", task);
				if(task != null) {
					task.run();
				}
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
	
}
