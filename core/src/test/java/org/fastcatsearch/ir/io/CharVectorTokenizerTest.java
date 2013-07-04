package org.fastcatsearch.ir.io;

import static org.junit.Assert.*;

import org.junit.Test;

public class CharVectorTokenizerTest {

	@Test
	public void testEmpty() {
		String str = "";
		CharVector term = new CharVector(str);
		CharVectorTokenizer tokenizer = new CharVectorTokenizer(term);
		
		if(tokenizer.hasNext()){
			assertFalse(true);
		}
		
	}
	
	@Test
	public void testMultiple() {
		String str = " abc   dsafsdfafs   한글입니다... ";
		CharVector term = new CharVector(str);
		CharVectorTokenizer tokenizer = new CharVectorTokenizer(term);
		
		if(tokenizer.hasNext()){
			CharVector token = tokenizer.next();
			assertEquals("abc", token.toString());
			
		}
		
		if(tokenizer.hasNext()){
			CharVector token = tokenizer.next();
			assertEquals("dsafsdfafs", token.toString());
			
		}
		
		if(tokenizer.hasNext()){
			CharVector token = tokenizer.next();
			assertEquals("한글입니다...", token.toString());
			
		}
	}

}
