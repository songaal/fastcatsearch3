package org.apache.lucene.analysis.core;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.Character.UnicodeBlock;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.fastcatsearch.ir.common.IRException;
import org.junit.Test;

public class TypeTokenizerTest {
	
	@Test
	public void testString(){
		
		char[] chars = "ㄱㄴㄷㅏㅠㅠ한engABC123-~!@#$%^&*()_".toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char ch =  chars[i];
			UnicodeBlock block = UnicodeBlock.of(ch);
			String type = TypeTokenizer.getType(ch);
			System.out.println(i+ " : " + ch +" : " + type + " : "+block);
		}
	}
	
	@Test
	public void testAll(){
		
		for (int i = 0; i < 1000; i++) {
			char ch =  (char) i;
			UnicodeBlock block = UnicodeBlock.of(ch);
			String type = TypeTokenizer.getType(ch);
			if(type == TypeTokenizer.UNCATEGORIZED){
				System.out.println(i+ " : " + ch +" : " + type + " : "+block);
			}
		}
	}
	
	@Test
	public void testTypeTokenizer() throws IOException {
		
		String text = null;
		text = "Allen H. Neuharth, 89, whose drive,  figures in modern journalism, died Friday in Florida.";
		text = "Mr. Neuharth’s death came after he suffered in Cocoa Beach, according to a story in I.O.C world USA Today, the national newspaper he founded.";
		text = "Neuharth, the onetime chief of the Gannett Co., a huge newspaper chain, also helped found the Newseum in Washington, and he achieved widespread recognition for the columns he wrote in USA Today, expressing his pungent views on public issues.";
		text = "Mr. Neuharth — a 5-foot-7 World War II combat hair — once dressed white, but with vivid color.";
		text = "(도쿄=연합뉴스) 조준형 특파원 = 18∼19일(이하 현지시간) 워싱턴DC에서 열린 주요 20개국(G20) 재무장관·중앙은행 총재 회의가 일본은행의 과감한 '돈풀기'를 양해하는 듯한 분위기에서 마무리되면서 엔화가치가 다시 하락했다. ";
		text = "In  $400 million. But a 1987 profile of Mr. Neuharth  paper’s fifth anniversary, of 5.5million. The paper could be found each weekday in the most remote parts of the nation, purchased from circulation boxes that also seemed designed for visual impact.";
		text = "According to the profile in People, USA Today was a newspaper that he had “conceived, designed, packaged and sold.”";
		text = "LTE-A와 갤럭시S4를 10/27일 한국외 10개국에서 출시한다.";
		text = "68.6cm(27형) / 와이드(16:9) / AH-IPS(광시야각) / 1920 x 1080 / 0.311mm / 5㎳ / 250cd / 1,000:1 / 20,000,000:1 / LED 방식 / 틸트 / 스피커 / 무결점 정책 / HDMI x2 / D-SUB / 32W / 0.3W / 절전기능(e-Saver) / 화면분할 기능(Screen+)";
//		text = "갤럭시S4 ABC-1H20 galaxy3 A.O.C 2757D";
//		text = "20229152";
//		text = "최우수    정품 123 대리점 갤럭시S4 알파스캔 AOC 2757 IPS LED 모니터/IPS/TV/27인치/엘지/삼성/컴퓨터/pc/HDMI/   VA";
//		text = "가/b/1344/asdlkj9saf/  dfㄴ언ㄹㄴㅇ /ㄴㅇ라머    ㅣㄴ어/  미ㅏ널;";
//		text = "ㅇ /ㄴㅇ라머    ㅣㄴ어/  미ㅏ널;";
		TypeTokenizer tokenizer = new TypeTokenizer(new StringReader(text));
		tokenizer.reset();
		TypeAttribute typeAttribute = tokenizer.getAttribute(TypeAttribute.class);
		PositionIncrementAttribute positionIncrementAttribute = tokenizer.getAttribute(PositionIncrementAttribute.class);
		
		System.out.println(text);
		CharTermAttribute charTermAttribute = tokenizer.getAttribute(CharTermAttribute.class);
		while(tokenizer.incrementToken()){
			System.out.println(">>"+charTermAttribute +" "+typeAttribute.type() +" " + positionIncrementAttribute.getPositionIncrement() );
		}
		
		
	}
	@Test
	public void testSpeed() throws IRException {
		TypeTokenizer tokenizer = new TypeTokenizer(null);
		TokenizerTestBase testBase = new TokenizerTestBase();
		testBase.testTokenizerSpeed(tokenizer, true);
	}
	
	@Test
	public void testSpeedWithFile() throws IRException {
//		File file = new File("/Users/swsong/Desktop/prodExtV1_9_sample.txt");
		File file = new File("/Users/swsong/tmp/prod.txt");
		TypeTokenizer tokenizer = new TypeTokenizer(null);
		TokenizerTestBase testBase = new TokenizerTestBase();
		testBase.testTokenizer(tokenizer, file, true);
	}
}
