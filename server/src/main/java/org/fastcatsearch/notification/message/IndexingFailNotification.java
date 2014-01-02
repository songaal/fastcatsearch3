package org.fastcatsearch.notification.message;

import java.sql.Timestamp;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class IndexingFailNotification extends IndexingFinishNotification {

	public IndexingFailNotification() {
	}

	public IndexingFailNotification(String collectionId, IndexingType indexingType, ResultStatus resultStatus, long startTime, long finishTime, Streamable result) {
		super("MSG-01002", collectionId, indexingType, resultStatus, startTime, finishTime, result);
	}

	@Override
	public String toMessageString() {
		Object[] params = new Object[5];
		params[0] = collectionId;
		params[1] = indexingType.toString();
		params[2] = new Timestamp(startTime).toString();
		params[3] = new Timestamp(finishTime).toString();

		if (result instanceof StreamableThrowable) {
			StreamableThrowable throwable = (StreamableThrowable) result;
			params[4] = "에러내역: " + throwable.getThrowable().toString();
		} else {
			params[4] = "실패결과: " + result.toString();
		}
		return getFormattedMessage(params);
	}
}
