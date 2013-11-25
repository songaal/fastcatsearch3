package org.fastcatsearch.http.writer;

import java.io.IOException;
import java.io.Writer;

import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 검색결과를 response stream에 기록하는 클래스.
 * */
public abstract class AbstractDocumentSearchResultWriter  {
	protected static Logger logger = LoggerFactory.getLogger(AbstractDocumentSearchResultWriter.class);
	protected Writer writer;
	
	public AbstractDocumentSearchResultWriter(Writer writer){
		this.writer = writer;
	}
	public abstract void writeResult(Object obj, ResponseWriter rStringer, long searchTime, boolean isSuccess) throws ResultWriterException, IOException;
}
