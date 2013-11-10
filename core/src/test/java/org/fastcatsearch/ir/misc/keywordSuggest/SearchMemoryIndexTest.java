package org.fastcatsearch.ir.misc.keywordSuggest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;

public class SearchMemoryIndexTest {

	@Test
	public void test() throws IOException {
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
				,"ㅋㅋ  재밌다"
		};
		SearchMemoryIndex index = new SearchMemoryIndex();
		for(String keyword : keywordList){
			index.add(keyword);
		}
		index.makeIndex();
		
		
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		System.out.print(">>");
		while((line = console.readLine()) != null){
			
//			String result = index.exactSearch(line);
			List<String> result = index.prefixSearch(line);
			if(result != null && result.size() > 0){
			System.out.println("-------------");
			for(String r : result){
				System.out.println("> "+r);
			}
			System.out.println("-------------");
			}else{
				System.out.println("No result.");
			}
			System.out.print(">>");
		}
		
	}

}
