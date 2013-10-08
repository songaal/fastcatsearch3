package org.fastcatsearch.processlogger;

import java.sql.Timestamp;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.SessionAndMapper;
import org.fastcatsearch.db.dao.IndexingHistory;
import org.fastcatsearch.db.dao.IndexingResult;
import org.fastcatsearch.db.mapper.IndexingHistoryMapper;
import org.fastcatsearch.db.vo.IndexingResultVO;
import org.fastcatsearch.db.vo.IndexingStatusVO;
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
				SessionAndMapper<IndexingHistoryMapper> sessionAndMapper = dbService.getSessionAndMapper(IndexingHistoryMapper.class);
				
				
				SessionAndMapper<IndexingHistoryMapper> sessionAndMapper2 = dbService.getSessionAndMapper(IndexingHistoryMapper.class);
				
				IndexingHistoryMapper mapper = sessionAndMapper.getMapper();
				
				if (log.getIndexingType() == IndexingType.FULL) {
					// 전체색인시는 증분색인 정보를 클리어해준다.
					//TODO 
					
					
//					indexingResult.delete(log.getCollection(), IndexingType.FULL);
				}
//				int result = indexingResult.updateOrInsert(log.getCollection(), log.getIndexingType(), IndexingResult.STATUS_RUNNING, 0, 0, 0,
//						log.isScheduled(), new Timestamp(log.getStartTime()), null, 0);
				
				IndexingStatusVO vo = new IndexingStatusVO();
				mapper.updateEntry(vo);
			}

		} else if (processLog instanceof IndexingFinishProcessLog) {
			//
			// 색인종료
			//
			IndexingFinishProcessLog log = (IndexingFinishProcessLog) processLog;

			DBService dbService = ServiceManager.getInstance().getService(DBService.class);
			if (dbService != null) {
				IndexingResult indexingResult = dbService.db().getDAO("IndexingResult", IndexingResult.class);
				IndexingHistory indexingHistory = dbService.db().getDAO("IndexingHistory", IndexingHistory.class);
				if (log.isSuccess()) {
					//
					// 색인 성공
					//
					IndexingJobResult indexingJobResult = (IndexingJobResult) log.getResult();
					indexingResult.updateResult(log.getCollectionId(), log.getIndexingType(), IndexingResult.STATUS_SUCCESS,
							indexingJobResult.indexStatus.getInsertCount(), indexingJobResult.indexStatus.getUpdateCount(),
							indexingJobResult.indexStatus.getDeleteCount(), new Timestamp(log.getEndTime()), (int) log.getDurationTime());
					indexingHistory.insert(log.getCollectionId(), log.getIndexingType(), true, indexingJobResult.indexStatus.getInsertCount(),
							indexingJobResult.indexStatus.getUpdateCount(), indexingJobResult.indexStatus.getDeleteCount(), log.isScheduled(),
							new Timestamp(log.getStartTime()), new Timestamp(log.getEndTime()), (int) log.getDurationTime());
				} else {
					//
					// 색인 실패
					//
					indexingResult.updateResult(log.getCollectionId(), log.getIndexingType(), IndexingResult.STATUS_FAIL, 0, 0, 0,
							new Timestamp(log.getEndTime()), (int) log.getDurationTime());

					indexingHistory.insert(log.getCollectionId(), log.getIndexingType(), false, 0, 0, 0, log.isScheduled(),
							new Timestamp(log.getStartTime()), new Timestamp(log.getEndTime()), (int) log.getDurationTime());
				}

			}
		}
	}
}
