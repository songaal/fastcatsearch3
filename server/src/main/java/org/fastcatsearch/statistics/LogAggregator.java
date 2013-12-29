package org.fastcatsearch.statistics;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.io.DirBufferedReader;
import org.fastcatsearch.statistics.log.AbstractLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogAggregator<LogType extends AbstractLog> {

	protected static Logger logger = LoggerFactory.getLogger(LogAggregator.class);

	private File[] inFileList;
	private String encoding;
	private List<LogAggregateHandler<LogType>> handlerList;

	public LogAggregator(File[] inFileList, String encoding, List<LogAggregateHandler<LogType>> handlerList) {
		this.inFileList = inFileList;
		this.encoding = encoding;
		this.handlerList = handlerList;
	}

	public void aggregate() {

		try {
			DirBufferedReader lineReader = new DirBufferedReader(inFileList, encoding);
			String line = null;
			while ((line = lineReader.readLine()) != null) {
//				logger.debug("line > {}", line);
				// 여러 핸들러가 수행한다.
				for (LogAggregateHandler<LogType> h : handlerList) {
					h.handleLog(line);
				}
			}
			
			for (LogAggregateHandler<LogType> h : handlerList) {
				h.done();
			}
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	
	public static class Counter {

		private int count;

		public Counter(int i) {
			count = i;
		}

		public void increment() {
			count++;
		}

		public int value() {
			return count;
		}
	}

}
