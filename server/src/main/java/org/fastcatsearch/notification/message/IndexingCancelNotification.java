package org.fastcatsearch.notification.message;

import java.sql.Timestamp;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.ir.common.IndexingType;

public class IndexingCancelNotification extends IndexingFinishNotification {
	
	public IndexingCancelNotification(){
	}
	
	public IndexingCancelNotification(String collectionId, IndexingType indexingType, ResultStatus resultStatus, long startTime, long finishTime, Streamable result) {
		super("MSG-01003", collectionId, indexingType, resultStatus, startTime, finishTime, result);
	}

	@Override
	public String toMessageString() {
		Object[] params = new Object[4];
		params[0] = collectionId;
		params[1] = indexingType.toString();
		params[2] = new Timestamp(startTime).toString();
		params[3] = new Timestamp(finishTime).toString();
		
		return getFormattedMessage(params);
	}
}
