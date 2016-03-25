package org.fastcatsearch.ir.search.method;

import java.io.IOException;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.search.BufferedPostingReader;
import org.fastcatsearch.ir.search.PostingReader;

public class NormalSearchMethod extends AbstractSearchMethod {

	@Override
	public PostingReader doSearch(String indexId, CharVector term, int termPosition, int weight, int segmentDocumentCount) throws IOException {

		if (memoryLexicon.size() == 0) {
			return null;
		}

		if (term.length() == 0) {
			return null;
		}

		long[] posInfo = new long[2];
		boolean found = memoryLexicon.binsearch(term, posInfo);

		long inputOffset = -1;
		// cannot find in memory index, let's find it in file index
		try {
			if (found) {
				inputOffset = posInfo[1];
			} else {
				lexiconInput.seek(posInfo[0]);
				while (lexiconInput.position() < lexiconFileLimit) {
					char[] term2 = lexiconInput.readUString();
					int cmp = compareKey(term2, term);

					if (cmp == 0) {
						inputOffset = lexiconInput.readLong();
						if (logger.isDebugEnabled()) {
							logger.debug("search success = {} at field-{}", term, indexId);
						}
						break;
					} else if (cmp > 0) {
						// if term value is greater than this term, there's no
						// such word. search fail
						if (logger.isDebugEnabled()) {
							logger.debug("search fail = {} at field[{}]", term, indexId);
						}
						break;
					} else {
						// skip reading inputOffset
						lexiconInput.seek(lexiconInput.position() + IOUtil.SIZE_OF_LONG);
					}
				}
			}
		} catch (IOException e) {
			logger.error("error while search index", e);
			throw e;
		}
		if (inputOffset >= 0) {
			return new BufferedPostingReader(term, termPosition, weight, segmentDocumentCount, indexFieldOption, postingInput, inputOffset);
		}

		return null;
	}

}
