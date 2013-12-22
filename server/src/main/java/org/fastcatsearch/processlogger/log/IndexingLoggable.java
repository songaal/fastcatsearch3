package org.fastcatsearch.processlogger.log;

import org.fastcatsearch.db.mapper.IndexingHistoryMapper;
import org.fastcatsearch.db.mapper.IndexingResultMapper;

public interface IndexingLoggable {
	public void writeLog(IndexingHistoryMapper indexingHistoryMapper, IndexingResultMapper indexingResultMapper);
}
