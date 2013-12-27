package org.fastcatsearch.statistics;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.fastcatsearch.ir.io.DirBufferedReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogAggregator<LogType extends AbstractLog> {

	protected static Logger logger = LoggerFactory.getLogger(LogAggregator.class);

	private File[] inFileList;
	private List<LogAggregateHandler<LogType>> logAggregateHandlerList;
	private int runSize;
	private Set<String> stopWords;
	private String encoding;

	public LogAggregator(File[] inFileList, List<LogAggregateHandler<LogType>> logAggregateHandlerList, int runSize, String encoding, Set<String> stopWords) {
		this.inFileList = inFileList;
		this.logAggregateHandlerList = logAggregateHandlerList;
		this.runSize = runSize;
		this.encoding = encoding;
		this.stopWords = stopWords;
	}


	public void aggregate(File outputFile) {

		try {
			DirBufferedReader lineReader = new DirBufferedReader(inFileList, encoding);
			String line = null;
			while ((line = lineReader.readLine()) != null) {
				logger.debug("line > {}", line);
				// 여러 핸들러가 수행한다.
				for (LogAggregateHandler<LogType> h : logAggregateHandlerList) {
					h.handleLog(line);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
