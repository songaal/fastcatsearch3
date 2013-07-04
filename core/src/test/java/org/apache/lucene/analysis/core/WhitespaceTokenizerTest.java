package org.apache.lucene.analysis.core;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.fastcatsearch.ir.common.IRException;
import org.junit.Test;

public class WhitespaceTokenizerTest {

	@Test
	public void testWhitespace(){
		System.out.println("FS > "+'\u001C');
		for(int i=0; i< 0x22; i++){
			if(Character.isWhitespace(i)){
				System.out.println(">> "+Integer.toHexString(i)+"["+(char)i+"]");
			}
		}
	}
	
	@Test
	public void testSpeed() throws IRException {
		WhitespaceTokenizer tokenizer = new WhitespaceTokenizer(null);
		TokenizerTestBase testBase = new TokenizerTestBase();
		testBase.testTokenizerSpeed(tokenizer, false);
	}

}
