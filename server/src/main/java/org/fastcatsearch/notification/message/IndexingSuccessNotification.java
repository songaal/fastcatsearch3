package org.fastcatsearch.notification.message;

import java.sql.Timestamp;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.job.result.IndexingJobResult;

public class IndexingSuccessNotification extends IndexingFinishNotification {

	public IndexingSuccessNotification() {
	}

	public IndexingSuccessNotification(String collectionId, IndexingType indexingType, String indexingStep, ResultStatus resultStatus, long startTime, long finishTime, Streamable result) {
		super("MSG-01001", collectionId, indexingType, indexingStep, resultStatus, startTime, finishTime, result);
	}

	@Override
	public String toMessageString() {
		String[] params = new String[6];
		params[0] = collectionId;
		params[1] = indexingType.toString();
		params[2] = indexingStep;
		params[3] = new Timestamp(startTime).toString();
		params[4] = new Timestamp(finishTime).toString();

		IndexingJobResult result2 = (IndexingJobResult) result;
		if (result2.indexStatus != null) {
			params[5] = "Documents[" + Integer.toString(result2.indexStatus.getDocumentCount()) + "]"
					+ "Deletes[" + Integer.toString(result2.indexStatus.getDeleteCount()) + "]";
		} else {
			params[5] = "Empty";
		}
		return getFormattedMessage(params);
	}
}
