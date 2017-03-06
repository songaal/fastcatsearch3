package org.fastcatsearch.notification.message;

import java.sql.Timestamp;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.ir.common.IndexingType;

public class IndexingCancelNotification extends IndexingFinishNotification {
	
	public IndexingCancelNotification(){
	}
	
	public IndexingCancelNotification(String collectionId, IndexingType indexingType, String indexingStep, ResultStatus resultStatus, long startTime, long finishTime, Streamable result) {
		super("MSG-01003", collectionId, indexingType, indexingStep, resultStatus, startTime, finishTime, result);
	}

	@Override
	public String toMessageString() {
		String[] params = new String[5];
		params[0] = collectionId;
		params[1] = indexingType.toString();
		params[2] = indexingStep;
		params[3] = new Timestamp(startTime).toString();
		params[4] = new Timestamp(finishTime).toString();
		
		return getFormattedMessage(params);
	}
}
