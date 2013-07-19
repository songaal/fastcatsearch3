package org.fastcatsearch.notification.message;

import java.io.IOException;
import java.sql.Timestamp;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class IndexingFinishNotification extends Notification {

	private String collection;
	private String indexingType;
	private boolean isSuccess;
	private long startTime;
	private long endTime;
	private Streamable result;

	public IndexingFinishNotification() { }
	
	public IndexingFinishNotification(String collection, String indexingType, boolean isSuccess, long startTime, long endTime,
			Streamable result) {
		super("MSG-01001");
		this.collection = collection;
		this.indexingType = indexingType;
		this.isSuccess = isSuccess;
		this.startTime = startTime;
		this.endTime = endTime;
		this.result = result;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
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
	public void writeTo(DataOutput output) throws IOException {
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
		Object[] params = new Object[6];
		params[0] = collection;
		params[1] = indexingType.equals(IndexingResult.TYPE_FULL_INDEXING)?"전체":"증분";
		params[2] = isSuccess?"성공":"실패";
		params[3] = new Timestamp(startTime).toString();
		params[4] = new Timestamp(endTime).toString();
		
		if(isSuccess){
			IndexingJobResult result2 = (IndexingJobResult) result;
			params[5] = "문서수["+Integer.toString(result2.docSize)+"]";
		}else{
			if(result instanceof StreamableThrowable){
				StreamableThrowable throwable = (StreamableThrowable) result;
				params[5] = "에러내역: "+throwable.getThrowable().toString();
			}else{
				params[5] = "실패결과: " + result.toString();
			}
		}
		return getFormattedMessage(params);
	}

}
