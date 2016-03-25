package org.fastcatsearch.ir.search.posting;

import org.fastcatsearch.ir.search.TermDocCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class NodeReader {
	protected static Logger logger = LoggerFactory.getLogger(NodeReader.class);
	
	// 다음 문서번호.
	public abstract int next() throws IOException;

	// 채워준다.
	public abstract void fill(TermDocCollector termDocCollector);

	public abstract void close();
	
}
