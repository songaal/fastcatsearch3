package org.fastcatsearch.notification.message;

import java.io.IOException;
import java.sql.Timestamp;

import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class IndexingStartNotification extends Notification {

	private String collectionId;
	private IndexingType indexingType;
	protected String indexingStep;
	private long startTime;
	private boolean isScheduled;

	public IndexingStartNotification() {
	}

	public IndexingStartNotification(String collectionId, IndexingType indexingType, String indexingStep, long startTime, boolean isScheduled) {
		super("MSG-01000");
		this.collectionId = collectionId;
		this.indexingType = indexingType;
		this.indexingStep = indexingStep;
		this.startTime = startTime;
		this.isScheduled = isScheduled;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		collectionId = input.readString();
		indexingType = IndexingType.valueOf(input.readString());
		indexingStep = input.readString();
		startTime = input.readLong();
		isScheduled = input.readBoolean();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		output.writeString(collectionId);
		output.writeString(indexingType.name());
		output.writeString(indexingStep);
		output.writeLong(startTime);
		output.writeBoolean(isScheduled);
	}

	@Override
	public String toString() {
		return "Indexing Started : collectionId[" + collectionId + "] type[" + indexingType + "] step[" + indexingStep+ "] isScheduled[" + isScheduled
				+ "]";
	}

	@Override
	public String toMessageString() {
		return getFormattedMessage(collectionId, indexingType.name(), indexingStep, isScheduled ? "Scheduled" : "Manual", new Timestamp(startTime).toString());
	}

}
