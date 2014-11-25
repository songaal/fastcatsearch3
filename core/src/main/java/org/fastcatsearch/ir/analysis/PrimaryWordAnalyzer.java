package org.fastcatsearch.ir.analysis;

import java.io.IOException;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.core.TypeTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 공백과 특수문자를 제외한 주요 단어만 뽑아낸다. 사전과 분석로직은 사용하지 않는다.
 * */
public class PrimaryWordAnalyzer extends Analyzer {

	private static final Logger logger = LoggerFactory.getLogger(PrimaryWordAnalyzer.class);

	public PrimaryWordAnalyzer() {
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {

		final TypeTokenizer tokenizer = new TypeTokenizer(reader);
//		try {
//			tokenizer.reset();
//		} catch (IOException e) {
//			logger.error("tokenizer reset error", e);
//		}
		// 분리된 어절을 하나씩 처리한다.

		final CharTermAttribute charTermAttribute = tokenizer.getAttribute(CharTermAttribute.class);
		final TypeAttribute typeAttribute = tokenizer.getAttribute(TypeAttribute.class);

		TokenFilter filter = new TokenFilter(tokenizer) {

			@Override
			public boolean incrementToken() throws IOException {
				boolean found = false;
				while (input.incrementToken()) {
					found = false;
					if (typeAttribute.type() == TypeTokenizer.SYMBOL) {
						// logger.debug("term : {}", charTermAttribute.toString());
						continue;
					}
					
					//길이제한은 없음.
					
//					if(typeAttribute.type() == TypeTokenizer.HANGUL && charTermAttribute.length() > 10){
//						continue;
//					}
//					
//					if(charTermAttribute.length() > 15){
//						continue;
//					}
					
					found = true;
					break;
				}
				
				return found;
			}
		};

		return new TokenStreamComponents(tokenizer, filter);
	}
}