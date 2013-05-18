package org.fastcatsearch.notification;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.notification.message.Notification;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class IndexingFinishNotification extends Notification {

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

	@Override
	public String toString() {
		return "색인시작됨 : 성공["+isSuccess+"] collection["+collection+"] type[" + indexingType+ "] 시작["+startTime+"] 종료["+startTime+"]";
	}

	@Override
	public String toMessageString() {
		// TODO Auto-generated method stub
		return null;
	}

}
