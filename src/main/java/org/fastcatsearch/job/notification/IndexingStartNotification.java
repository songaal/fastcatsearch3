package org.fastcatsearch.job.notification;

import java.io.IOException;
import java.sql.Timestamp;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;

public class IndexingStartNotification extends StreamableJob {
	private static final long serialVersionUID = 1084526563289625615L;
	private String collection;
	private String indexingType;
	private long startTime;
	private boolean isScheduled;

	public IndexingStartNotification(String collection, String indexingType, long startTime, boolean isScheduled) {
		this.collection = collection;
		this.indexingType = indexingType;
		this.startTime = startTime;
		this.isScheduled = isScheduled;
	}

	@Override
	public JobResult doRun() throws JobException, ServiceException {

		DBService dbService = ServiceManager.getInstance().getService(DBService.class);
		if (dbService != null) {

			IndexingResult indexingResult = dbService.getDAO("IndexingResult", IndexingResult.class);
			if (indexingType == IndexingResult.TYPE_FULL_INDEXING) {
				// 전체색인시는 증분색인 정보를 클리어해준다.
				indexingResult.delete(collection, IndexingResult.TYPE_INC_INDEXING);
			}
			int result = indexingResult.updateOrInsert(collection, indexingType, IndexingResult.STATUS_RUNNING, 0, 0, 0,
					isScheduled, new Timestamp(startTime), null, 0);

			if (result > 0) {
				return new JobResult(true);
			}
		}

		return new JobResult(false);
	}

	@Override
	public void readFrom(StreamInput input) throws IOException {
		collection = input.readString();
		indexingType = input.readString();
		startTime = input.readLong();
		isScheduled = input.readBoolean();
	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeString(collection);
		output.writeString(indexingType);
		output.writeLong(startTime);
		output.writeBoolean(isScheduled);
	}

}
