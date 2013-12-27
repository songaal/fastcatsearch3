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

/**
 * LogReader를 이용해 handleLog(String line) 를 통해 들어온 로그 1줄을 파싱해 읽어들여,
 * aggregateMap에 카운트를 올려준다. (통계작업)
 * 메모리가 커질수 있으므로, aggregateMap.size()가 미리정해놓은 runSize보다 커지면 파일로 flush한다.
 * flush한 run파일은 comparator 를 통해 정렬하여 기록된다.
 * 마지막 done()호출시 run들을 파일병합하여 하나의 정렬된 파일로 만든다.
 * 
 * 하위클래스에서는 source 로그파일을 읽어들이는 reader를 정의하고, output을 위한 comparator 를 정의한다. 
 * 
 * */
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
