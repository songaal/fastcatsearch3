package org.fastcatsearch.statistics.util;

import java.io.File;

import org.fastcatsearch.statistics.AbstractLog;
import org.fastcatsearch.statistics.LogAggregator;
import org.fastcatsearch.statistics.LogAggregator.Counter;

public abstract class LogWriter<LogType extends AbstractLog> {

	public LogWriter(File file){
		
	}
	
	public void close() {
		
	}

	public abstract void formatLog(String key, Counter value);

}
