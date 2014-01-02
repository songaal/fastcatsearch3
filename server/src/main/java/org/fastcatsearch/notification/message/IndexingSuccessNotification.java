package org.fastcatsearch.notification.message;

import java.sql.Timestamp;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.job.result.IndexingJobResult;

public class IndexingSuccessNotification extends IndexingFinishNotification {

	public IndexingSuccessNotification() {
	}

	public IndexingSuccessNotification(String collectionId, IndexingType indexingType, ResultStatus resultStatus, long startTime, long finishTime, Streamable result) {
		super("MSG-01001", collectionId, indexingType, resultStatus, startTime, finishTime, result);
	}

	@Override
	public String toMessageString() {
		Object[] params = new Object[5];
		params[0] = collectionId;
		params[1] = indexingType.toString();
		params[2] = new Timestamp(startTime).toString();
		params[3] = new Timestamp(finishTime).toString();

		IndexingJobResult result2 = (IndexingJobResult) result;
		if (result2.indexStatus != null) {
			params[4] = "추가문서수[" + Integer.toString(result2.indexStatus.getInsertCount()) + "] " + "업데이트문서수[" + Integer.toString(result2.indexStatus.getUpdateCount()) + "]"
					+ "삭제문서수[" + Integer.toString(result2.indexStatus.getDeleteCount()) + "]";
		} else {
			params[4] = "Empty";
		}
		return getFormattedMessage(params);
	}
}
