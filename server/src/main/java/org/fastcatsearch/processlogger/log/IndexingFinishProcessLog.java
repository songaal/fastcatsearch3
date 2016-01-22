package org.fastcatsearch.processlogger.log;

import java.io.IOException;
import java.sql.Timestamp;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingHistoryMapper;
import org.fastcatsearch.db.mapper.IndexingResultMapper;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.db.vo.IndexingStatusVO;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class IndexingFinishProcessLog implements ProcessLog, IndexingLoggable {

	private String collectionId;
	private IndexingType indexingType;
	private String indexingStep;
	private ResultStatus resultStatus;
	private long startTime;
	private long endTime;
	private boolean isScheduled;
	private Streamable result;

	public IndexingFinishProcessLog() { }
	
	public IndexingFinishProcessLog(String collectionId, IndexingType indexingType, String indexingStep, ResultStatus resultStatus, long startTime, long endTime,
			boolean isScheduled, Streamable result) {
		this.collectionId = collectionId;
		this.indexingType = indexingType;
		this.indexingStep = indexingStep;
		this.resultStatus = resultStatus;
		this.startTime = startTime;
		this.endTime = endTime;
		this.isScheduled = isScheduled;
		this.result = result;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public IndexingType getIndexingType() {
		return indexingType;
	}

	public String getIndexingStep() {
		return indexingStep;
	}
	
	public ResultStatus getResultStatus() {
		return resultStatus;
	}
	
	public boolean isSuccess() {
		return resultStatus == ResultStatus.SUCCESS;
	}
	
	public boolean isCanceled() {
		return resultStatus == ResultStatus.CANCEL;
	}
	
	public boolean isFail() {
		return resultStatus == ResultStatus.FAIL;
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
		indexingStep = input.readString();
		resultStatus = ResultStatus.valueOf(input.readString());
		startTime = input.readLong();
		endTime = input.readLong();
		isScheduled = input.readBoolean();
		if(input.readBoolean()){
			if (!isFail()) {
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
		output.writeString(indexingStep);
		output.writeString(resultStatus.name());
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
	
	
	public void writeLog(IndexingHistoryMapper indexingHistoryMapper, IndexingResultMapper indexingResultMapper){

		IndexingStatusVO vo = new IndexingStatusVO();
		if (!isFail()) {
			//
			// 색인 성공
			//
			IndexingJobResult indexingJobResult = (IndexingJobResult) getResult();
			vo.collectionId = getCollectionId();
			vo.type = getIndexingType();
			vo.step = getIndexingStep();
			vo.status = getResultStatus();
			vo.isScheduled = isScheduled();
			if(indexingJobResult.indexStatus != null){
				vo.docSize = indexingJobResult.indexStatus.getDocumentCount();
				vo.deleteSize = indexingJobResult.indexStatus.getDeleteCount();
			}
			vo.startTime = new Timestamp(getStartTime());
			vo.endTime = new Timestamp(getEndTime());
			vo.duration = getDurationTime();

		} else {
			//
			// 색인 실패
			//
			vo.collectionId = getCollectionId();
			vo.type = getIndexingType();
			vo.step = getIndexingStep();
			vo.status = getResultStatus();
			vo.isScheduled = isScheduled();
			vo.startTime = new Timestamp(getStartTime());
			vo.endTime = new Timestamp(getEndTime());
			vo.duration = getDurationTime();
		}

		try {
			//색인결과는 취소와 정지가 아닐경우에만 업데이트한다. 실패는 색인파일에 영향을 줄수 있으므로 표기한다.  
			if(vo.status != ResultStatus.CANCEL && vo.status != ResultStatus.STOP){
				if (indexingType == IndexingType.FULL) {
					indexingResultMapper.deleteEntry(collectionId, IndexingType.FULL);
					indexingResultMapper.deleteEntry(collectionId, IndexingType.ADD);
				}else if(indexingType == IndexingType.ADD){
					indexingResultMapper.deleteEntry(collectionId, IndexingType.ADD);
				}else{
					indexingResultMapper.deleteEntry(collectionId, indexingType);
				}
				indexingResultMapper.putEntry(vo);
			}
			indexingHistoryMapper.putEntry(vo);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

}
