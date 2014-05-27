package org.fastcatsearch.ir.analysis;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 2-gram과 3-gram으로 뽑아낸다.
 * */
public class NGramWordAnalyzer extends Analyzer {

	private static final Logger logger = LoggerFactory.getLogger(NGramWordAnalyzer.class);

	public NGramWordAnalyzer() {
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {

		final NGramWordTokenizer tokenizer = new NGramWordTokenizer(reader, 2, 3);

		TokenFilter filter = new StandardFilter(tokenizer);

		return new TokenStreamComponents(tokenizer, filter);
	}
}