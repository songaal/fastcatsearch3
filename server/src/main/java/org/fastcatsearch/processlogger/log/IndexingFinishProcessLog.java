package org.fastcatsearch.processlogger.log;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class IndexingFinishProcessLog implements ProcessLog {

	private String collectionId;
	private String shardId;
	private IndexingType indexingType;
	private boolean isSuccess;
	private long startTime;
	private long endTime;
	private boolean isScheduled;
	private Streamable result;

	public IndexingFinishProcessLog() { }
	
	public IndexingFinishProcessLog(String collectionId, String shardId, IndexingType indexingType, boolean isSuccess, long startTime, long endTime,
			boolean isScheduled, Streamable result) {
		this.collectionId = collectionId;
		this.shardId = shardId;
		this.indexingType = indexingType;
		this.isSuccess = isSuccess;
		this.startTime = startTime;
		this.endTime = endTime;
		this.isScheduled = isScheduled;
		this.result = result;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public String getShardId() {
		return shardId;
	}
	
	public IndexingType getIndexingType() {
		return indexingType;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public boolean isScheduled() {
		return isScheduled;
	}

	public Streamable getResult() {
		return result;
	}
	
	public int getDurationTime() {
		return (int) (endTime - startTime);
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		collectionId = input.readString();
		indexingType = IndexingType.valueOf(input.readString());
		isSuccess = input.readBoolean();
		startTime = input.readLong();
		endTime = input.readLong();
		isScheduled = input.readBoolean();
		if(input.readBoolean()){
			if (isSuccess) {
				result = new IndexingJobResult();
			} else {
				result = new StreamableThrowable();
			}
			result.readFrom(input);
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(collectionId);
		output.writeString(indexingType.name());
		output.writeBoolean(isSuccess);
		output.writeLong(startTime);
		output.writeLong(endTime);
		output.writeBoolean(isScheduled);
		if(result == null) {
			output.writeBoolean(false);
		}else{
			output.writeBoolean(true);
			result.writeTo(output);
		}
	}
	

}
