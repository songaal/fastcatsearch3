package org.fastcatsearch.ir.analysis;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

public class PrimaryWordAnalyzerTest {

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

}
