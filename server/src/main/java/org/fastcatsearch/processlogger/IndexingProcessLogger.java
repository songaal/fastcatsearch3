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

			//색인시작시는 indexing result를 변경하지 않는다. 차후 실패나, 정지를 할수 있으므로..
//			DBService dbService = ServiceManager.getInstance().getService(DBService.class);
//			if (dbService != null) {
//				MapperSession<IndexingResultMapper> resultMapperSession = dbService.getMapperSession(IndexingResultMapper.class);
//
//				try {
//					IndexingResultMapper indexingResultMapper = resultMapperSession.getMapper();
//
//					// 전체색인시는 증분색인 정보까지 클리어해준다.
//					if (log.getIndexingType() == IndexingType.FULL) {
//						indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.FULL);
//					}
//					indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.ADD);
//
//					IndexingStatusVO vo = new IndexingStatusVO();
//					vo.collectionId = log.getCollectionId();
//					vo.type = log.getIndexingType();
//					vo.status = ResultStatus.RUNNING;
//					vo.isScheduled = log.isScheduled();
//					vo.startTime = new Timestamp(log.getStartTime());
//					vo.endTime = new Timestamp(log.getStartTime());// 없으면 derby
//																	// 에러발생.
//					try {
//						indexingResultMapper.putEntry(vo);
//					} catch (Exception e) {
//						logger.error("", e);
//					}
//				} finally {
//					if (resultMapperSession != null) {
//						resultMapperSession.closeSession();
//					}
//				}
//			}

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
//					if (log.getIndexingType() == IndexingType.FULL) {
//						indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.FULL);
//					}
//					indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.ADD);

					IndexingStatusVO vo = new IndexingStatusVO();
					if (!log.isFail()) {
						//
						// 색인 성공
						//
						IndexingJobResult indexingJobResult = (IndexingJobResult) log.getResult();
						vo.collectionId = log.getCollectionId();
						vo.type = log.getIndexingType();
						vo.status = log.getResultStatus();
						vo.isScheduled = log.isScheduled();
						if(indexingJobResult.indexStatus != null){
							vo.docSize = indexingJobResult.indexStatus.getDocumentCount();
							vo.insertSize = indexingJobResult.indexStatus.getInsertCount();
							vo.updateSize = indexingJobResult.indexStatus.getUpdateCount();
							vo.deleteSize = indexingJobResult.indexStatus.getDeleteCount();
						}
						vo.startTime = new Timestamp(log.getStartTime());
						vo.endTime = new Timestamp(log.getEndTime());
						vo.duration = log.getDurationTime();

					} else {
						//
						// 색인 실패
						//
						vo.collectionId = log.getCollectionId();
						vo.type = log.getIndexingType();
						vo.status = log.getResultStatus();
						vo.isScheduled = log.isScheduled();
						vo.startTime = new Timestamp(log.getStartTime());
						vo.endTime = new Timestamp(log.getEndTime());
						vo.duration = log.getDurationTime();
					}

					try {
						//색인결과는 취소와 정지가 아닐경우에만 업데이트한다. 실패는 색인파일에 영향을 줄수 있으므로 표기한다.  
						if(vo.status != ResultStatus.CANCEL && vo.status != ResultStatus.STOP){
							if (log.getIndexingType() == IndexingType.FULL) {
								indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.FULL);
								indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.ADD);
							}else if(log.getIndexingType() == IndexingType.ADD){
								indexingResultMapper.deleteEntry(log.getCollectionId(), IndexingType.ADD);
							}
							indexingResultMapper.putEntry(vo);
						}
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
