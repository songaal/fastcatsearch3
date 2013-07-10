package org.fastcatsearch.ir.summary;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.io.CharVector;
import org.junit.Test;

public class HighlightingTest {

	@Test
	public void test() throws Exception {
		
		Class cls = Class.forName("com.fastcatsearch.plugin.analysis.ko.standard.StandardKoreanAnalyzerTest");
		
		Method m = cls.getMethod("initDic");
		
		Object obj = cls.newInstance();
		
		m.invoke(obj);
		
		
		BasicHighlightAndSummary highlighter = new BasicHighlightAndSummary();
		
    	String str1 = "일이라면 언제나 가장 열성적으로 저를 지지해주는 후원자이기도 하셨습니다. 어린 시절부터 저는 집안의 장남으로서 동생과 부모님을 돌보며 자신의 것만을 고집하기보다는 다른 사람들과 화합하는 방법들을 자연스럽게 터득하며 성장하였습니다. 이러한 저의 가정환경은 훗날 제가 책임감이 강하고 성실한 사람으로 발전하";
    	String str2 = "어린 시절부터 집안의 장남으로서 동생을 돌보며 자신의 것만을 고집하기보다는 다른 사람들과 화합하는 방법들을 자연스럽게 터득하며 성장하였습니다. ";
    	
		
//    	String str1 = "Hello this is a piece of text that is very long and contains too much preamble and the meat is really here which says kennedy has been shot";
//    	String str2 = "hello this is a piece of text contains much been shot";
//    	str1 = "감사노트도 적어보고..그러다가 세월에, 여건에 휩쓸려 잊고 살았는데..또 우연히 네빌 고다드의 5일간의 강의 라는 책을 읽고 믿음으로 걸어라를 읽고 드뎌 사람들이 ..";
//    	str2 = "네빌 고다드의 5일간의 강의 라는 책";
//
//    	str2 = "어린";
//    	
//    	//str2 = "일이라면 언제나 열성적으로 저를 후원자이기도 하셨습니다. 어린 시절부터 저는 집안의 장남으로서 동생과 부모님을 돌보며 자신의 것만을 고집하기보다는 다른 사람들과 화합하는 방법들을 자연스럽게 터득하며 성장하였습니다. 이러한 저의 가정환경은 훗날 제가 책임감이 강하고 성실한 사람으로 발전하";
//    	//IRSettings.setHome(pArgs[0]);
//    	//Dic.init();
//			//Dic.set("korean", new File("/home/lupfeliz/Documents/workspace/fastcat_basic_server/release/fastcat_basic/dic/korean.dic"));
//			//Dic.set("userword", new File("/home/lupfeliz/Documents/workspace/fastcat_basic_server/release/fastcat_basic/dic/user.dic"));
//			Dic.set("korean", new File("/home/lupfeliz/fastcatsearch/dic/korean.dic"));
//			Dic.set("userword", new File("/home/lupfeliz/fastcatsearch/dic/user.dic"));
//			Dic.makeApdbStopDic();
//			Dic.makeStopDic();
//			
//			CharVector token = new CharVector();
//			Tokenizer tokenizer = new ExtendedKoreanTokenizer();
//			
//			tokenizer.setInput(str2.toCharArray());
//			
//			List<String>terms = new ArrayList<String>();
//			
//			while(tokenizer.nextToken(token)) {
//				System.out.println(token);
//				terms.add(token.toString());
//			}
//			
//    	System.out.println(highlighter.highlight(tokenizer,str1,terms,"[","]",30));
	}

}
