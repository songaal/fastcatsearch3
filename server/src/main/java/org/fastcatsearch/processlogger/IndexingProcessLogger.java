package org.fastcatsearch.processlogger;

import org.fastcatsearch.db.DBService;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.IndexingHistoryMapper;
import org.fastcatsearch.db.mapper.IndexingResultMapper;
import org.fastcatsearch.processlogger.log.IndexingLoggable;
import org.fastcatsearch.processlogger.log.ProcessLog;
import org.fastcatsearch.service.ServiceManager;

public class IndexingProcessLogger implements ProcessLogger {

	@Override
	public void log(ProcessLog processLog) {
		
		//색인 결과 및 상태를 로깅한다.
		if (processLog instanceof IndexingLoggable) {
			DBService dbService = ServiceManager.getInstance().getService(DBService.class);
			if (dbService != null) {
				MapperSession<IndexingHistoryMapper> historyMapperSession = dbService.getMapperSession(IndexingHistoryMapper.class);
				MapperSession<IndexingResultMapper> resultMapperSession = dbService.getMapperSession(IndexingResultMapper.class);
				try {
					IndexingHistoryMapper indexingHistoryMapper = historyMapperSession.getMapper();
					IndexingResultMapper indexingResultMapper = resultMapperSession.getMapper();
					IndexingLoggable indexingLoggable = (IndexingLoggable) processLog;
					indexingLoggable.writeLog(indexingHistoryMapper, indexingResultMapper);
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
