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

public class NormalizedKeywordAnalyzer extends Analyzer {
	
	private static final Logger logger = LoggerFactory.getLogger(NormalizedKeywordAnalyzer.class);
	
	public NormalizedKeywordAnalyzer() {
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName,
			Reader reader) {
		
		final TypeTokenizer tokenizer = new TypeTokenizer(reader);
		try {
			tokenizer.reset();
		} catch (IOException e) {
			logger.error("tokenizer reset error",e );
		}
		//분리된 어절을 하나씩 처리한다.
		
		final CharTermAttribute charTermAttribute = tokenizer.getAttribute(CharTermAttribute.class);
		final TypeAttribute typeAttribute = tokenizer.getAttribute(TypeAttribute.class);

		TokenFilter filter = new TokenFilter(tokenizer)  {

			@Override
			public boolean incrementToken() throws IOException {
				while(input.incrementToken()) {
					if(typeAttribute.type()!=TypeTokenizer.SYMBOL) {
						logger.debug("term : {}", charTermAttribute.toString());
						return true;
					}
				}
				return false;
			}
		};
		
		return new TokenStreamComponents(tokenizer,filter);
	}
}