package org.fastcatsearch.notification.message;

import java.sql.Timestamp;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class IndexingFailNotification extends IndexingFinishNotification {

	public IndexingFailNotification() {
	}

	public IndexingFailNotification(String collectionId, IndexingType indexingType, String indexingStep, ResultStatus resultStatus, long startTime, long finishTime, Streamable result) {
		super("MSG-01002", collectionId, indexingType, indexingStep, resultStatus, startTime, finishTime, result);
	}

	@Override
	public String toMessageString() {
		String[] params = new String[6];
		params[0] = collectionId;
		params[1] = indexingType.toString();
		params[2] = indexingStep;
		params[3] = new Timestamp(startTime).toString();
		params[4] = new Timestamp(finishTime).toString();

		if (result instanceof StreamableThrowable) {
			StreamableThrowable throwable = (StreamableThrowable) result;
			params[5] = "Error: " + throwable.getThrowable().toString();
		} else {
			params[5] = "Fail result: " + (result != null ? result.toString() : "");
		}
		return getFormattedMessage(params);
	}
}
