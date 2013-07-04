/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.analysis;

import junit.framework.TestCase;

import org.fastcatsearch.ir.analysis.EnglishTokenizer;
import org.fastcatsearch.ir.analysis.FieldTokenizer;
import org.fastcatsearch.ir.analysis.TabedTokenizer;
import org.fastcatsearch.ir.analysis.Tokenizer;
import org.fastcatsearch.ir.analysis.TypeTokenizer;
import org.fastcatsearch.ir.analysis.WhitespaceTokenizer;
import org.fastcatsearch.ir.io.CharVector;


public class TokenizerTest extends TestCase{
	public void testWhitespace(){
		WhitespaceTokenizer tokenizer = new WhitespaceTokenizer();
		tokenizer.setInput("하나  두울 셋 넷	     	다섯  여섯    일곱 여덟 아홉   열  ".toCharArray());
		CharVector token = new CharVector();
		while(tokenizer.nextToken(token)){
			System.out.println("Whitespace="+token.toString());
		}
	}
	
	public void testDefault(){
		FieldTokenizer tokenizer = new FieldTokenizer();
		tokenizer.setInput(" 하나  두울 셋 넷 다섯  여섯 일곱 여덟 아홉   열 ".toCharArray());
		CharVector token = new CharVector();
		while(tokenizer.nextToken(token)){
			System.out.println("Default="+token.toString());
		}
	}
	
	public void testTabedTokenizer(){
		FieldTokenizer tokenizer = new TabedTokenizer();
		String input = "1	10	100	100%	100%ㅈ	100%지	100%지ㅎ	100%지하	100%지하 	100%지하 ㅊ	100%지하 처	100%지하 천	100%지하 천ㅇ	100%지하 천여	100%지하 천연	100%지하 천연ㅇ	100%지하 천연아	100%지하 천연암	100%지하 천연암ㅂ	100%지하 천연암바	100%지하 천연암반	100%지하 천연암반ㅅ	100%지하 천연암반수";
		tokenizer.setInput(input.toCharArray());
		CharVector token = new CharVector();
		while(tokenizer.nextToken(token)){
			System.out.println("Default='"+token.toString()+"'");
		}
	}
	
	public void testEnglish(){
		Tokenizer tokenizer = new EnglishTokenizer();
//		String input = "I am thirty-two-years-old 12-in man";
		String input = "By SUDEEP REDDY And SARA MURRAYBrisk hiring in February pushed the U.S. unemployment rate below 9% for";
		input = "blends and nano-SiO2 micronanotech sss !nano adfsjl @nano on nanofibrous scaffolds";
		input = "SiO2 micro는 device 하다.";
		tokenizer.setInput(input.toCharArray());
		CharVector token = new CharVector();
		while(tokenizer.nextToken(token)){
			System.out.println(">>'"+token.toString()+"'");
		}
	}
	
	public void testTypeTokenizer(){
		TypeTokenizer tokenizer = new TypeTokenizer();
//		String input = "I am thirty-two-years-old 12-in man";
//		String input = "삼성전자 휴대폰 SS-ANY123초저가";
		String input = "로버트 셰클리의 ‘생활의 대가’를 읽고 쓴 독서감상문입니다.참고하셔서 도움이 되시기를 바랍니다.";
		input = "iphone_16g";
		tokenizer.setInput(input.toCharArray());
		CharVector token = new CharVector();
		while(tokenizer.nextToken(token)){
			System.out.println(">>'"+token.toString()+"'");
		}
	}
	
	public void test2TypeTokenizer(){
		TypeTokenizer tokenizer = new TypeTokenizer();
		String input = "기업간 M&A T-mobile ABC$ lupfeliz@gmail.com 60% 2/1 가. 어쩌구저쩌구..";
		tokenizer.setInput(input.toCharArray());
		CharVector token = new CharVector();
		while(tokenizer.nextToken(token)){
			System.out.println(">>'"+token.toString()+"'");
		}
	}
}
