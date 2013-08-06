package org.fastcatsearch.notification.message;

import java.io.IOException;
import java.sql.Timestamp;

import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class IndexingStartNotification extends Notification {

	private String collection;
	private IndexingType indexingType;
	private long startTime;
	private boolean isScheduled;

	public IndexingStartNotification() {
	}

	public IndexingStartNotification(String collection, IndexingType indexingType, long startTime, boolean isScheduled) {
		super("MSG-01000");
		this.collection = collection;
		this.indexingType = indexingType;
		this.startTime = startTime;
		this.isScheduled = isScheduled;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		collection = input.readString();
		indexingType = IndexingType.valueOf(input.readString());
		startTime = input.readLong();
		isScheduled = input.readBoolean();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collection);
		output.writeString(indexingType.name());
		output.writeLong(startTime);
		output.writeBoolean(isScheduled);
	}

	@Override
	public String toString() {
		return "색인시작됨 : collection[" + collection + "] type[" + indexingType + "] 자동[" + isScheduled + "]";
	}

	@Override
	public String toMessageString() {
		return getFormattedMessage(collection, indexingType == IndexingType.FULL_INDEXING ? "전체" : "증분", isScheduled ? "스케쥴" : "수동", new Timestamp(
				startTime).toString());
	}

}
