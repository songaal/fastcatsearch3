package org.fastcatsearch.ir.misc.keywordSuggest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class KeywordSuggestTest {

	@Test
	public void test() {
		String[] keywordList = new String[]{
				"지마켓 할인 특가"
				,"지마켓 마일리지 두배"
				,"지마켓 무이자"
				,"지마켓 합병"
				,"지마켓 네이버"
				,"지마켓 의류"
				,"옥션 지마켓"
				,"옥션 어바웃"
				,"네이버 지마켓"
				,"전기 요금 카드 납부"
				,"천 번을 흔들려야 어른이 된다."
		};
		SuggestDictionary suggestDictionary = new SuggestDictionary(keywordList);
		suggestDictionary.findSuggest("ㅈ");
		suggestDictionary.findSuggest("지");
		suggestDictionary.findSuggest("지마켓");
		suggestDictionary.findSuggest("지마켓 ");
		suggestDictionary.findSuggest("지마켓 ㅎ");
	}
	
	public static class SuggestDictionary {
		Map<String, Next> map = new HashMap<String, Next>();
		AtomicInteger keywordIdGenerator = new AtomicInteger();
		AtomicInteger eojeolIdGenerator = new AtomicInteger();
		
		public SuggestDictionary(String[] keywordList){
			for(String keyword : keywordList){
				String[] eojeolList = keyword.split(" ");
				int keywordId = keywordIdGenerator.getAndIncrement();
				for(String eojeol : eojeolList){
					Next next = new Next();
					int eojeolId = eojeolIdGenerator.getAndIncrement();
					map.put(eojeol, next);
				}
				
			}
		}
		
		public List<String> findSuggest(String keyword){
			List<String> suggestList = new ArrayList<String>();
			System.out.println("----- ["+keyword+"] -----");
			
			
			
			
			
			int i = 1;
			for(String suggestKeyword : suggestList){
				System.out.println(i++ +" : ["+suggestKeyword+"]");
			}
			return suggestList;
		}
		
	}
	
	public static class Next {
		
		
	}

}
