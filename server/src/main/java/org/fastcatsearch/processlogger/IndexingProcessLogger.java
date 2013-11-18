package org.fastcatsearch.processlogger;

import java.sql.Timestamp;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.IndexingHistoryMapper;
import org.fastcatsearch.db.mapper.IndexingResultMapper;
import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;
import org.fastcatsearch.db.vo.IndexingStatusVO;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.processlogger.log.IndexingFinishProcessLog;
import org.fastcatsearch.processlogger.log.IndexingStartProcessLog;
import org.fastcatsearch.processlogger.log.ProcessLog;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexingProcessLogger implements ProcessLogger {
	private static final Logger logger = LoggerFactory.getLogger(IndexingProcessLogger.class);

	@Override
	public void log(ProcessLog processLog) {
		if (processLog instanceof IndexingStartProcessLog) {
			IndexingStartProcessLog log = (IndexingStartProcessLog) processLog;

			DBService dbService = ServiceManager.getInstance().getService(DBService.class);
			if (dbService != null) {
				MapperSession<IndexingResultMapper> resultMapperSession = dbService.getMapperSession(IndexingResultMapper.class);

				try {
					IndexingResultMapper indexingResultMapper = resultMapperSession.getMapper();

					// 전체색인시는 증분색인 정보까지 클리어해준다.
					if (log.getIndexingType() == IndexingType.FULL) {
						indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.FULL);
					}
					indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.ADD);

					IndexingStatusVO vo = new IndexingStatusVO();
					vo.collectionId = log.getCollectionId();
					vo.type = log.getIndexingType();
					vo.status = ResultStatus.RUNNING;
					vo.isScheduled = log.isScheduled();
					vo.startTime = new Timestamp(log.getStartTime());
					vo.endTime = new Timestamp(log.getStartTime());// 없으면 derby
																	// 에러발생.
					try {
						indexingResultMapper.putEntry(vo);
					} catch (Exception e) {
						logger.error("", e);
					}
				} finally {
					if (resultMapperSession != null) {
						resultMapperSession.closeSession();
					}
				}
			}

		} else if (processLog instanceof IndexingFinishProcessLog) {
			//
			// 색인종료
			//
			IndexingFinishProcessLog log = (IndexingFinishProcessLog) processLog;

			DBService dbService = ServiceManager.getInstance().getService(DBService.class);
			if (dbService != null) {
				MapperSession<IndexingHistoryMapper> historyMapperSession = dbService.getMapperSession(IndexingHistoryMapper.class);
				MapperSession<IndexingResultMapper> resultMapperSession = dbService.getMapperSession(IndexingResultMapper.class);
				try {
					IndexingHistoryMapper indexingHistoryMapper = historyMapperSession.getMapper();
					IndexingResultMapper indexingResultMapper = resultMapperSession.getMapper();

					// 전체색인시는 증분색인 정보까지 클리어해준다.
					if (log.getIndexingType() == IndexingType.FULL) {
						indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.FULL);
					}
					indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.ADD);

					IndexingStatusVO vo = new IndexingStatusVO();
					if (log.isSuccess()) {
						//
						// 색인 성공
						//
						IndexingJobResult indexingJobResult = (IndexingJobResult) log.getResult();
						vo.collectionId = log.getCollectionId();
						vo.type = log.getIndexingType();
						vo.status = ResultStatus.SUCCESS;
						vo.isScheduled = log.isScheduled();
						vo.docSize = indexingJobResult.indexStatus.getDocumentCount();
						vo.insertSize = indexingJobResult.indexStatus.getInsertCount();
						vo.updateSize = indexingJobResult.indexStatus.getUpdateCount();
						vo.deleteSize = indexingJobResult.indexStatus.getDeleteCount();
						vo.startTime = new Timestamp(log.getStartTime());
						vo.endTime = new Timestamp(log.getEndTime());
						vo.duration = log.getDurationTime();

					} else {
						//
						// 색인 실패
						//
						vo.collectionId = log.getCollectionId();
						vo.type = log.getIndexingType();
						vo.status = ResultStatus.FAIL;
						vo.isScheduled = log.isScheduled();
						vo.startTime = new Timestamp(log.getStartTime());
						vo.endTime = new Timestamp(log.getEndTime());
						vo.duration = log.getDurationTime();
					}

					try {
						indexingResultMapper.putEntry(vo);
						indexingHistoryMapper.putEntry(vo);
					} catch (Exception e) {
						logger.error("", e);
					}
				} finally {
					if (historyMapperSession != null) {
						historyMapperSession.closeSession();
					}
					if (resultMapperSession != null) {
						resultMapperSession.closeSession();
					}
				}

			}
		}
	}
}
