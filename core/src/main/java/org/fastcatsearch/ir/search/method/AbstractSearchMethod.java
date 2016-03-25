package org.fastcatsearch.ir.search.method;

import org.fastcatsearch.ir.index.IndexFieldOption;
import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IndexInput;
import org.fastcatsearch.ir.search.MemoryLexicon;
import org.fastcatsearch.ir.search.PostingReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractSearchMethod implements SearchMethod {
	
	protected static Logger logger = LoggerFactory.getLogger(AbstractSearchMethod.class);
	
	protected IndexInput lexiconInput;
	
	protected IndexInput postingInput;
	
	protected MemoryLexicon memoryLexicon;

	protected IndexFieldOption indexFieldOption;
	
	protected long lexiconFileLimit;
	
	protected int segmentDocumentCount;
	
	public AbstractSearchMethod() {
	}

	public void init(MemoryLexicon memoryLexicon, IndexInput lexiconInput, IndexInput postingInput, IndexFieldOption indexFieldOption, int segmentDocumentCount) {
		this.memoryLexicon = memoryLexicon;
		this.lexiconInput = lexiconInput;
		this.postingInput = postingInput;
		this.indexFieldOption = indexFieldOption;
		this.segmentDocumentCount = segmentDocumentCount;
		
		this.lexiconFileLimit = lexiconInput.length();
	}
	
	protected int compareKey(char[] t, CharVector term) {

		int len1 = t.length;
		int len2 = term.length();

		int len = len1 < len2 ? len1 : len2;

		for (int i = 0; i < len; i++) {
			char ch = term.charAt(i);

			if (t[i] != ch) {
				return t[i] - ch;
			}
		}

		return len1 - len2;
	}
	
	protected abstract PostingReader doSearch(String indexId, CharVector term, int termPosition, int weight, int segmentDocumentCount) throws IOException;
		
	@Override
	public PostingReader search(String indexId, CharVector term, int termPosition, int weight) throws IOException {
		try{
			return doSearch(indexId, term, termPosition, weight, segmentDocumentCount);
		}finally{
			lexiconInput.clone();
		}
		
	}
	
}
