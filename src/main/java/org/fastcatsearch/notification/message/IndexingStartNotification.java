package org.fastcatsearch.notification.message;

import java.io.IOException;
import java.sql.Timestamp;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.db.dao.IndexingResult;

public class IndexingStartNotification extends Notification {
	
	private String collection;
	private String indexingType;
	private long startTime;
	private boolean isScheduled;

	public IndexingStartNotification() { }
	
	public IndexingStartNotification(String collection, String indexingType, long startTime, boolean isScheduled) {
		super("MSG-01000");
		this.collection = collection;
		this.indexingType = indexingType;
		this.startTime = startTime;
		this.isScheduled = isScheduled;
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

	@Override
	public String toString() {
		return "색인시작됨 : collection["+collection+"] type[" + indexingType+ "] 자동["+isScheduled+"]";
	}

	@Override
	public String toMessageString() {
		return getFormattedMessage(collection, indexingType.equals(IndexingResult.TYPE_FULL_INDEXING)?"전체":"증분", isScheduled?"스케쥴":"수동", new Timestamp(startTime).toString());
	}

}
