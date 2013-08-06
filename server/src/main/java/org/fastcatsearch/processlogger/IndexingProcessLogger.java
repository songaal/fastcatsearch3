package org.fastcatsearch.processlogger;

import java.sql.Timestamp;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.dao.IndexingHistory;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.processlogger.log.IndexingFinishProcessLog;
import org.fastcatsearch.processlogger.log.IndexingStartProcessLog;
import org.fastcatsearch.processlogger.log.ProcessLog;
import org.fastcatsearch.service.ServiceManager;

public class IndexingProcessLogger implements ProcessLogger {

	@Override
	public void log(ProcessLog processLog) {
		if (processLog instanceof IndexingStartProcessLog) {
			IndexingStartProcessLog log = (IndexingStartProcessLog) processLog;

			DBService dbService = ServiceManager.getInstance().getService(DBService.class);
			if (dbService != null) {

				IndexingResult indexingResult = dbService.getDAO("IndexingResult", IndexingResult.class);
				if (log.getIndexingType() == IndexingType.FULL) {
					// 전체색인시는 증분색인 정보를 클리어해준다.
					indexingResult.delete(log.getCollection(), IndexingType.FULL);
				}
				int result = indexingResult.updateOrInsert(log.getCollection(), log.getIndexingType(), IndexingResult.STATUS_RUNNING, 0, 0, 0,
						log.isScheduled(), new Timestamp(log.getStartTime()), null, 0);

			}

		} else if (processLog instanceof IndexingFinishProcessLog) {
			//
			// 색인종료
			//
			IndexingFinishProcessLog log = (IndexingFinishProcessLog) processLog;

			DBService dbService = ServiceManager.getInstance().getService(DBService.class);
			if (dbService != null) {
				IndexingResult indexingResult = dbService.getDAO("IndexingResult", IndexingResult.class);
				IndexingHistory indexingHistory = dbService.getDAO("IndexingHistory", IndexingHistory.class);
				if (log.isSuccess()) {
					//
					// 색인 성공
					//
					IndexingJobResult indexingJobResult = (IndexingJobResult) log.getResult();
					indexingResult.updateResult(log.getCollection(), log.getIndexingType(), IndexingResult.STATUS_SUCCESS,
							indexingJobResult.revisionInfo.getInsertCount(), indexingJobResult.revisionInfo.getUpdateCount(),
							indexingJobResult.revisionInfo.getDeleteCount(), new Timestamp(log.getEndTime()), (int) log.getDurationTime());
					indexingHistory.insert(log.getCollection(), log.getIndexingType(), true, indexingJobResult.revisionInfo.getInsertCount(),
							indexingJobResult.revisionInfo.getUpdateCount(), indexingJobResult.revisionInfo.getDeleteCount(), log.isScheduled(),
							new Timestamp(log.getStartTime()), new Timestamp(log.getEndTime()), (int) log.getDurationTime());
				} else {
					//
					// 색인 실패
					//
					indexingResult.updateResult(log.getCollection(), log.getIndexingType(), IndexingResult.STATUS_FAIL, 0, 0, 0,
							new Timestamp(log.getEndTime()), (int) log.getDurationTime());

					indexingHistory.insert(log.getCollection(), log.getIndexingType(), false, 0, 0, 0, log.isScheduled(),
							new Timestamp(log.getStartTime()), new Timestamp(log.getEndTime()), (int) log.getDurationTime());
				}

			}
		}
	}
}
