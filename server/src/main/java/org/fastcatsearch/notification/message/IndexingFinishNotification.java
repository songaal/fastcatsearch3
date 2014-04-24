package org.fastcatsearch.notification.message;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public abstract class IndexingFinishNotification extends Notification {

	protected String collectionId;
	protected IndexingType indexingType;
	protected String indexingStep;
	protected ResultStatus resultStatus;
	protected long startTime;
	protected long finishTime;
	protected Streamable result;

	public IndexingFinishNotification() { }
	
	public IndexingFinishNotification(String messageCode, String collection, IndexingType indexingType, String indexingStep, ResultStatus resultStatus, long startTime, long finishTime,
			Streamable result) {
		super(messageCode);
		this.collectionId = collection;
		this.indexingType = indexingType;
		this.indexingStep = indexingStep;
		this.resultStatus = resultStatus;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.result = result;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		collectionId = input.readString();
		indexingType = IndexingType.valueOf(input.readString());
		indexingStep = input.readString();
		resultStatus = ResultStatus.valueOf(input.readString());
		startTime = input.readLong();
		finishTime = input.readLong();
		if(input.readBoolean()){
			if (resultStatus != ResultStatus.FAIL) {
				result = new IndexingJobResult();
			} else {
				result = new StreamableThrowable();
			}
			result.readFrom(input);
		}
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		output.writeString(collectionId);
		output.writeString(indexingType.name());
		output.writeString(indexingStep);
		output.writeString(resultStatus.name());
		output.writeLong(startTime);
		output.writeLong(finishTime);
		if(result == null) {
			output.writeBoolean(false);
		}else{
			output.writeBoolean(true);
			result.writeTo(output);
		}
	}

	@Override
	public String toString() {
		return "Indexing Finished : resultStatus["+resultStatus+"] collectionId["+collectionId+"] type[" + indexingType+ "] step[" + indexingStep+ "] start["+startTime+"] finish["+finishTime+"]";
	}


}
