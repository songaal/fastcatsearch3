package org.fastcatsearch.ir.search.method;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.search.PostingReader;

public interface SearchMethod {
	
	public PostingReader search(String indexId, CharVector term, int termPosition, float weight);
}
