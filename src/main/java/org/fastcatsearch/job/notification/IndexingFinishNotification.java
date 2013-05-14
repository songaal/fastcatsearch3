package org.fastcatsearch.job.notification;

import java.io.IOException;
import java.sql.Timestamp;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.IndexingHistory;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.job.Job.JobResult;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class IndexingFinishNotification extends StreamableJob {

	private static final long serialVersionUID = 3802606441623422765L;
	private String collection;
	private String indexingType;
	private boolean isSuccess;
	private long startTime;
	private long endTime;
	private Streamable result;

	public IndexingFinishNotification(String collection, String indexingType, boolean isSuccess, long startTime, long endTime,
			Streamable result) {
		this.collection = collection;
		this.indexingType = indexingType;
		this.isSuccess = isSuccess;
		this.startTime = startTime;
		this.endTime = endTime;
		this.result = result;
	}

	@Override
	public JobResult doRun() throws JobException, ServiceException {

		DBService dbService = ServiceManager.getInstance().getService(DBService.class);
		if (dbService != null) {
			IndexingResult indexingResult = dbService.getDAO("IndexingResult", IndexingResult.class);
			IndexingHistory indexingHistory = dbService.getDAO("IndexingHistory", IndexingHistory.class);
			if (isSuccess) {
				IndexingJobResult indexingJobResult = (IndexingJobResult) result;
				indexingResult.updateResult(collection, indexingType, IndexingResult.STATUS_SUCCESS, indexingJobResult.docSize,
						indexingJobResult.updateSize, indexingJobResult.deleteSize, new Timestamp(endTime),
						(int) (endTime - startTime));
				indexingHistory.insert(collection, indexingType, true, indexingJobResult.docSize, indexingJobResult.updateSize,
						indexingJobResult.deleteSize, isScheduled(), new Timestamp(startTime), new Timestamp(endTime),
						(int) (endTime - startTime));
			} else {
				
				indexingResult.updateResult(collection, indexingType, IndexingResult.STATUS_FAIL, -1, -1, -1, new Timestamp(endTime),
						(int) (endTime - startTime));
				
				indexingHistory.insert(collection, indexingType, false, -1, -1, -1, isScheduled(), new Timestamp(startTime),
						new Timestamp(endTime), (int) (endTime - startTime));
			}

		}
		//무조건 true
		return new JobResult(true);
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		collection = input.readString();
		indexingType = input.readString();
		isSuccess = input.readBoolean();
		startTime = input.readLong();
		endTime = input.readLong();
		if(isSuccess){
			result = new IndexingJobResult();
		}else{
			result = new StreamableThrowable();
		}
		result.readFrom(input);
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeString(collection);
		output.writeString(indexingType);
		output.writeBoolean(isSuccess);
		output.writeLong(startTime);
		output.writeLong(endTime);
		result.writeTo(output);
	}

}
