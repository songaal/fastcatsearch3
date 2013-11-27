package org.fastcatsearch.notification.message;

import java.io.IOException;
import java.sql.Timestamp;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class IndexingFinishNotification extends Notification {

	private String collectionId;
	private IndexingType indexingType;
	private boolean isSuccess;
	private long startTime;
	private long finishTime;
	private Streamable result;

	public IndexingFinishNotification() { }
	
	public IndexingFinishNotification(String collection,IndexingType indexingType, boolean isSuccess, long startTime, long finishTime,
			Streamable result) {
		super("MSG-01001");
		this.collectionId = collection;
		this.indexingType = indexingType;
		this.isSuccess = isSuccess;
		this.startTime = startTime;
		this.finishTime = finishTime;
		this.result = result;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		super.readFrom(input);
		collectionId = input.readString();
		indexingType = IndexingType.valueOf(input.readString());
		isSuccess = input.readBoolean();
		startTime = input.readLong();
		finishTime = input.readLong();
		if(isSuccess){
			result = new IndexingJobResult();
		}else{
			result = new StreamableThrowable();
		}
		result.readFrom(input);
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		super.writeTo(output);
		output.writeString(collectionId);
		output.writeString(indexingType.name());
		output.writeBoolean(isSuccess);
		output.writeLong(startTime);
		output.writeLong(finishTime);
		result.writeTo(output);
	}

	@Override
	public String toString() {
		return "Indexing Started! : isSuccess["+isSuccess+"] collectionId["+collectionId+"] type[" + indexingType+ "] start["+startTime+"] finish["+finishTime+"]";
	}

	@Override
	public String toMessageString() {
		Object[] params = new Object[6];
		params[0] = collectionId;
		params[1] = (indexingType == IndexingType.FULL)?"전체":"증분";
		params[2] = isSuccess?"성공":"실패";
		params[3] = new Timestamp(startTime).toString();
		params[4] = new Timestamp(finishTime).toString();
		
		if(isSuccess){
			IndexingJobResult result2 = (IndexingJobResult) result;
			if(result2.indexStatus != null){
				params[5] = "추가문서수["+Integer.toString(result2.indexStatus.getInsertCount())+"] " +
					"업데이트문서수["+Integer.toString(result2.indexStatus.getUpdateCount())+"]" +
					"삭제문서수["+Integer.toString(result2.indexStatus.getDeleteCount())+"]";
			}else{
				params[5] = "Empty";
			}
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
