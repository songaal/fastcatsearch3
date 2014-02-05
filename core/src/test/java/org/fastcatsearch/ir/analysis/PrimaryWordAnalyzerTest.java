package org.fastcatsearch.ir.analysis;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrimaryWordAnalyzerTest {
	
	private static final Logger logger = LoggerFactory.getLogger(PrimaryWordAnalyzerTest.class);

	@Test
	public void test() throws IOException {
		PrimaryWordAnalyzer analyzer = new PrimaryWordAnalyzer();
		String text = "서울 지하철(300만명)";
		TokenStream tokenStream = analyzer.tokenStream("", new StringReader(text));
		CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		assertTrue(tokenStream.incrementToken());
		assertEquals("서울", charTermAttribute.toString());
		
		assertTrue(tokenStream.incrementToken());
		assertEquals("지하철", charTermAttribute.toString());
		
		assertTrue(tokenStream.incrementToken());
		assertEquals("300", charTermAttribute.toString());
		
		assertTrue(tokenStream.incrementToken());
		assertEquals("만명", charTermAttribute.toString());
		
		assertFalse(tokenStream.incrementToken());
			
	}
	
	@Test
	public void testSingle() throws IOException {
		Analyzer analyzer;// = new PrimaryWordAnalyzer();
		//analyzer = new PrimaryWordAnalyzer();
		analyzer = new WhitespaceAnalyzer();
		String text = "nt x170";
		
		TokenStream tokenStream = analyzer.tokenStream("", new StringReader(text));
		CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
		
		tokenStream.reset();
		for(;tokenStream.incrementToken(); ) {
			logger.debug("char:{}", charTermAttribute);
		}
	}

}
