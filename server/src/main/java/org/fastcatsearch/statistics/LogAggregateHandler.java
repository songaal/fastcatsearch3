package org.fastcatsearch.statistics;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.fastcatsearch.statistics.LogAggregator.Counter;
import org.fastcatsearch.statistics.util.LogReader;
import org.fastcatsearch.statistics.util.AggregationResultWriter;
import org.fastcatsearch.statistics.util.SortedFileMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LogAggregateHandler<LogType extends AbstractLog> {
	protected static Logger logger = LoggerFactory.getLogger(LogAggregateHandler.class);
	
	private LogReader<LogType> logReader;
	private Comparator<String> comparator;
	private int runSize;
	private Map<String, Counter> aggregateMap;
	private int runCount;
	 String encoding;
	 
	public LogAggregateHandler(LogReader<LogType> logReader, Comparator<String> comparator, int runSize, String encoding) {
		this.logReader = logReader;
		this.comparator = comparator;
		this.runSize = runSize;
		aggregateMap = new HashMap<String, Counter>(runSize);
		this.encoding = encoding;
	}

	protected abstract AggregationResultWriter newLogWriter(File file);

	public void handleLog(String line) throws IOException {
		LogType log = logReader.readLine(line);
		if (log != null) {
			Counter counter = aggregateMap.get(log.getKey());
			if (counter != null) {
				counter.increment();
			} else {
				aggregateMap.put(log.getKey(), new Counter(1));
			}
		}

		// 메모리에 쌓인갯수 체크하여 run생성.
		if (aggregateMap.size() >= runSize) {
			flushRun();
		}
	}

	private void flushRun() throws IOException {
		File runFile = new File(runCount++ + ".log");
		TreeMap<String, Counter> sortedMap = new TreeMap<String, Counter>(comparator);
		sortedMap.putAll(aggregateMap);
		AggregationResultWriter logWriter = newLogWriter(runFile);
		try {
			for (Map.Entry<String, Counter> entry : sortedMap.entrySet()) {
				logWriter.write(entry.getKey(), entry.getValue());
			}
		} finally {
			if (logWriter != null) {
				logWriter.close();
			}
		}
	}
	
	public void done() throws IOException{
		if(aggregateMap.size() > 0){
			flushRun();
		}
		
		//run을 모아서 하나로 만든다.
		
		File outputFile = null;
		File[] runFileList = null;
		SortedFileMerger merger = new SortedFileMerger(runFileList, outputFile, comparator);
		merger.merge();
		
	}

}
