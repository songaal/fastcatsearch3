package org.fastcatsearch.servlet;

import java.io.IOException;

import org.fastcatsearch.util.ResultWriter;
import org.fastcatsearch.util.StringifyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 검색결과를 ResultWriter에 기록하는 클래스.
 * */
public abstract class AbstractSearchResultWriter {
	protected static Logger logger = LoggerFactory.getLogger(AbstractSearchResultWriter.class);
	protected ResultWriter resultWriter;
	
	public AbstractSearchResultWriter(ResultWriter resultStringer){
		this.resultWriter = resultStringer;
	}
	public abstract void writeResult(Object obj, long searchTime, boolean isSuccess) throws StringifyException, IOException;
}
