package org.fastcatsearch.processlogger.log;

import java.io.IOException;

import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class IndexingStartProcessLog implements ProcessLog {

	private String collectionId;
	private String shardId;
	private IndexingType indexingType;
	private long startTime;
	private boolean isScheduled;

	public IndexingStartProcessLog(){ }
	
	public IndexingStartProcessLog(String collection, String shardId, IndexingType indexingType, long startTime, boolean isScheduled) {
		this.collectionId = collection;
		this.shardId = shardId;
		this.indexingType = indexingType;
		this.startTime = startTime;
		this.isScheduled = isScheduled;
	}

	public String getCollection() {
		return collectionId;
	}

	public String getShardId() {
		return shardId;
	}

	public IndexingType getIndexingType() {
		return indexingType;
	}

	public long getStartTime() {
		return startTime;
	}

	public boolean isScheduled() {
		return isScheduled;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		shardId = input.readString();
		indexingType = IndexingType.valueOf(input.readString());
		startTime = input.readLong();
		isScheduled = input.readBoolean();
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeString(shardId);
		output.writeString(indexingType.name());
		output.writeLong(startTime);
		output.writeBoolean(isScheduled);
	}

}
