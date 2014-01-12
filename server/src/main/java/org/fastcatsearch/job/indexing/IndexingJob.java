package org.fastcatsearch.job.indexing;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.ir.CollectionIndexerable;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.state.IndexingTaskKey;
import org.fastcatsearch.job.state.IndexingTaskState;
import org.fastcatsearch.job.state.TaskStateService;
import org.fastcatsearch.notification.NotificationService;
import org.fastcatsearch.notification.message.IndexingCancelNotification;
import org.fastcatsearch.notification.message.IndexingFailNotification;
import org.fastcatsearch.notification.message.IndexingFinishNotification;
import org.fastcatsearch.notification.message.IndexingStartNotification;
import org.fastcatsearch.notification.message.IndexingSuccessNotification;
import org.fastcatsearch.processlogger.IndexingProcessLogger;
import org.fastcatsearch.processlogger.ProcessLoggerService;
import org.fastcatsearch.processlogger.log.IndexingFinishProcessLog;
import org.fastcatsearch.processlogger.log.IndexingStartProcessLog;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IndexingJob extends Job implements Streamable {
	protected static Logger indexingLogger = LoggerFactory.getLogger("INDEXING_LOG");

	private static final long serialVersionUID = -3449580683698711296L;

	protected String collectionId;
	protected IndexingType indexingType;

	private ProcessLoggerService processLoggerService;
	private NotificationService notificationService;

	private TaskStateService taskStateService;

	protected IndexingTaskState indexingTaskState;
	private long indexingStartTime;
	protected boolean stopRequested; // 색인중단요청..

	protected CollectionIndexerable indexer;

	private IndexingTaskKey indexingTaskKey;
	
	public long indexingStartTime() {
		return indexingStartTime;
	}

	public void requestStop() {
		logger.info("Collection [{}] Indexing Job Stop Requested! ", collectionId);
		stopRequested = true;
		if (indexer != null) {
			indexer.requestStop();
		}
		indexingTaskState.addState(IndexingTaskState.STATE_STOP_REQUESTED);
	}

	public void prepare(IndexingType indexingType) {
		collectionId = getStringArgs();
		this.indexingType = indexingType;

		ServiceManager serviceManager = ServiceManager.getInstance();
		processLoggerService = serviceManager.getService(ProcessLoggerService.class);
		notificationService = serviceManager.getService(NotificationService.class);
		taskStateService = serviceManager.getService(TaskStateService.class);
	}

	protected void updateIndexingStatusStart() {
		indexingLogger.info("[{}] {} Indexing Start! {}", collectionId, indexingType.name(), getClass().getSimpleName());
		indexingStartTime = System.currentTimeMillis();
		processLoggerService.log(IndexingProcessLogger.class, new IndexingStartProcessLog(collectionId, indexingType, jobStartTime(), isScheduled()));
		notificationService.sendNotification(new IndexingStartNotification(collectionId, indexingType, jobStartTime(), isScheduled()));
		indexingTaskKey = new IndexingTaskKey(collectionId, indexingType);
		indexingTaskState = (IndexingTaskState) taskStateService.register(indexingTaskKey);
		indexingTaskState.start();
	}

	protected void updateIndexingStatusFinish(ResultStatus resultStatus, Streamable streamableResult) {
		long endTime = System.currentTimeMillis();

		processLoggerService.log(IndexingProcessLogger.class, new IndexingFinishProcessLog(collectionId, indexingType, resultStatus, indexingStartTime, endTime, isScheduled(),
				streamableResult));
		IndexingFinishNotification indexingFinishNotification = null;
		if (resultStatus == ResultStatus.SUCCESS) {
			indexingFinishNotification = new IndexingSuccessNotification(collectionId, indexingType, resultStatus, indexingStartTime, endTime, streamableResult);
		} else if (resultStatus == ResultStatus.FAIL) {
			indexingFinishNotification = new IndexingFailNotification(collectionId, indexingType, resultStatus, indexingStartTime, endTime, streamableResult);
		} else if (resultStatus == ResultStatus.CANCEL) {
			indexingFinishNotification = new IndexingCancelNotification(collectionId, indexingType, resultStatus, indexingStartTime, endTime, streamableResult);
		}
		
		if(indexingFinishNotification != null){
			notificationService.sendNotification(indexingFinishNotification);
		}
		indexingTaskState.finish();
		taskStateService.remove(indexingTaskKey);
		indexingLogger.info("[{}] {} Indexing Finish!", collectionId, indexingType.name());
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		args = input.readString();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString((String) args);
	}
}
