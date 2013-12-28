package org.fastcatsearch.statistics.util;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface AggregationResultWriter {
	public static Logger logger = LoggerFactory.getLogger(AggregationResultWriter.class);
	
	//취합된 키, count가 넘어온다.
	public void write(String key, int count) throws IOException;

	//리소스를 닫는다.
	public void close();
}
