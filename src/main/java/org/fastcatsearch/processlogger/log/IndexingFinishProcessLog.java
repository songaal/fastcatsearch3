package org.fastcatsearch.processlogger.log;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class IndexingFinishProcessLog implements ProcessLog {

	private String collection;
	private String indexingType;
	private boolean isSuccess;
	private long startTime;
	private long endTime;
	private boolean isScheduled;
	private Streamable result;

	public IndexingFinishProcessLog() { }
	
	public IndexingFinishProcessLog(String collection, String indexingType, boolean isSuccess, long startTime, long endTime,
			boolean isScheduled, Streamable result) {
		this.collection = collection;
		this.indexingType = indexingType;
		this.isSuccess = isSuccess;
		this.startTime = startTime;
		this.endTime = endTime;
		this.isScheduled = isScheduled;
		this.result = result;
	}

	public String getCollection() {
		return collection;
	}

	public String getIndexingType() {
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
	public void readFrom(StreamInput input) throws IOException {
		collection = input.readString();
		indexingType = input.readString();
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
	public void writeTo(StreamOutput output) throws IOException {
		output.writeString(collection);
		output.writeString(indexingType);
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
