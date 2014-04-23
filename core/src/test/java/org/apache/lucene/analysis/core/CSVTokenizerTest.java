package org.apache.lucene.analysis.core;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

public class CSVTokenizerTest {

	@Test
	public void testCSV() throws IOException{
		String str ="abc, sdf, 1234, 한글 이다. , ㅁㅁㅁ";
		CSVAnalyzer a = new CSVAnalyzer();
		Reader reader = new StringReader(str);
		TokenStream tokenStream = a.tokenStream("", reader);
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		tokenStream.reset();
		while(tokenStream.incrementToken()){
			System.out.println(charTermAttribute.toString());
		}
	}
	

}
