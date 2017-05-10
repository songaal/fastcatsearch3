package org.fastcatsearch.notification.message;

import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

import java.io.IOException;
import java.sql.Timestamp;

public class IndexingTimeoutNotification extends Notification {

	private String collectionId;
	private IndexingType indexingType;
	private long startTime;
	private boolean isScheduled;
	private int timeout;

	public IndexingTimeoutNotification() {
	}

	public IndexingTimeoutNotification(String collectionId, IndexingType indexingType, long startTime, boolean isScheduled, int timeoutInSecond) {
		super("MSG-01004");
		this.collectionId = collectionId;
		this.indexingType = indexingType;
		this.startTime = startTime;
		this.isScheduled = isScheduled;
		this.timeout = timeoutInSecond;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		collectionId = input.readString();
		indexingType = IndexingType.valueOf(input.readString());
		startTime = input.readLong();
		isScheduled = input.readBoolean();
		timeout = input.readInt();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		output.writeString(collectionId);
		output.writeString(indexingType.name());
		output.writeLong(startTime);
		output.writeBoolean(isScheduled);
		output.writeInt(timeout);
	}

	@Override
	public String toString() {
		return "Indexing Timeout : collectionId[" + collectionId + "] type[" + indexingType + "] isScheduled[" + isScheduled
				+ "] timeout" + timeout;
	}

	@Override
	public String toMessageString() {
		return getFormattedMessage(collectionId, indexingType.name(), isScheduled ? "Scheduled" : "Manual", new Timestamp(startTime).toString(), String.valueOf(timeout));
	}

}
