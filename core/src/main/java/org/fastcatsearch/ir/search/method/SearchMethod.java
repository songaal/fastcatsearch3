package org.fastcatsearch.ir.search.method;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.search.PostingReader;

import java.io.IOException;

public interface SearchMethod {
	
	public PostingReader search(String indexId, CharVector term, int termPosition, int weight) throws IOException;
}
