package org.fastcatsearch.notification;

import java.io.IOException;
import java.sql.Timestamp;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.IndexingHistory;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class IndexingFinishNotificationbak extends StreamableJob {

	private static final long serialVersionUID = 3802606441623422765L;
	private String collection;
	private String indexingType;
	private boolean isSuccess;
	private long startTime;
	private long endTime;
	private Streamable result;

	public IndexingFinishNotificationbak(String collection, String indexingType, boolean isSuccess, long startTime, long endTime,
			Streamable result) {
		this.collection = collection;
		this.indexingType = indexingType;
		this.isSuccess = isSuccess;
		this.startTime = startTime;
		this.endTime = endTime;
		this.result = result;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {

		//색인 결과를 기록할때 에러발생하여 너무 빨리 작업이 끝난경우 finish가 start보다 먼저 기록되어 상태가 "색인중"으로 남을 수 있기 때문에 잠시쉬어준다.
		//색인알림은 리턴을 기다리지 않기때문에 조금 지연되어도 문제없다. @swsong
		try {
			Thread.sleep(300);
		} catch (InterruptedException ignore) {
		}
		
		DBService dbService = ServiceManager.getInstance().getService(DBService.class);
		if (dbService != null) {
			IndexingResult indexingResult = dbService.getDAO("IndexingResult", IndexingResult.class);
			IndexingHistory indexingHistory = dbService.getDAO("IndexingHistory", IndexingHistory.class);
			if (isSuccess) {
				IndexingJobResult indexingJobResult = (IndexingJobResult) result;
//				indexingResult.updateResult(collection, indexingType, IndexingResult.STATUS_SUCCESS, indexingJobResult.docSize,
//						indexingJobResult.updateSize, indexingJobResult.deleteSize, new Timestamp(endTime),
//						(int) (endTime - startTime));
//				indexingHistory.insert(collection, indexingType, true, indexingJobResult.docSize, indexingJobResult.updateSize,
//						indexingJobResult.deleteSize, isScheduled(), new Timestamp(startTime), new Timestamp(endTime),
//						(int) (endTime - startTime));
			} else {
				
//				indexingResult.updateResult(collection, indexingType, IndexingResult.STATUS_FAIL, 0, 0, 0, new Timestamp(endTime),
//						(int) (endTime - startTime));
//				
//				indexingHistory.insert(collection, indexingType, false, 0, 0, 0, isScheduled(), new Timestamp(startTime),
//						new Timestamp(endTime), (int) (endTime - startTime));
			}

		}
		
		//notification관련 셋팅을 얻어와서 통지를 수행한다.
		environment.settingManager().getServerSettings();
		
		
		//무조건 true
		return new JobResult(true);
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

}
