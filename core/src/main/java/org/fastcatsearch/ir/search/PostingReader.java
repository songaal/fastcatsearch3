package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.io.CharVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 스트림으로 부터 포스팅을 순차적으로 읽는다.
 * */
public interface PostingReader {
	public static Logger logger = LoggerFactory.getLogger(PostingReader.class);
	
	public int size();

	public int weight();
	
	public boolean hasNext() throws IOException;

	public PostingDoc next() throws IOException;

	public void close();

	public int termPosition();

	public CharVector term();

	public int documentCount();
}
