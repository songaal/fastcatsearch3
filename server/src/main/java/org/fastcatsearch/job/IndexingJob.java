package org.fastcatsearch.job;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.notification.message.IndexingFinishNotification;
import org.fastcatsearch.notification.message.IndexingStartNotification;
import org.fastcatsearch.processlogger.IndexingProcessLogger;
import org.fastcatsearch.processlogger.ProcessLoggerService;
import org.fastcatsearch.processlogger.log.IndexingFinishProcessLog;
import org.fastcatsearch.processlogger.log.IndexingStartProcessLog;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class IndexingJob extends Job {
	protected static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");
	
	private static final long serialVersionUID = -3449580683698711296L;
	
	protected String collectionId;
	protected IndexingType indexingType;
	
	private ProcessLoggerService processLoggerService;
	private NotificationService notificationService;
	private long indexingStartTime;
	
	public long indexingStartTime(){
		return indexingStartTime;
	}
	
	public void prepare(IndexingType indexingType){
		String[] args = getStringArrayArgs();
		collectionId = args[0];
		this.indexingType = indexingType;
		
		ServiceManager serviceManager = ServiceManager.getInstance();
		processLoggerService = serviceManager.getService(ProcessLoggerService.class);
		notificationService = serviceManager.getService(NotificationService.class);
		
	}
	
	protected void updateIndexingStatusStart(){
		indexingLogger.info("[{}] {} Indexing Start! {}", collectionId, indexingType.name(), getClass().getSimpleName());
		indexingStartTime = System.currentTimeMillis();
		processLoggerService.log(IndexingProcessLogger.class, new IndexingStartProcessLog(collectionId,
				indexingType, jobStartTime(), isScheduled()));
		notificationService.notify(new IndexingStartNotification(collectionId, indexingType,
				jobStartTime(), isScheduled()));
	}
	
	protected void updateIndexingStatusFinish(boolean isSuccess, Streamable streamableResult){
		long endTime = System.currentTimeMillis();
		
		processLoggerService.log(IndexingProcessLogger.class, new IndexingFinishProcessLog(collectionId,
				indexingType, isSuccess, indexingStartTime, endTime, isScheduled(), streamableResult));

		notificationService.notify(new IndexingFinishNotification(collectionId, indexingType, isSuccess,
				indexingStartTime, endTime, streamableResult));
		indexingLogger.info("[{}] {} Indexing Finish!", collectionId, indexingType.name());
	}
}
