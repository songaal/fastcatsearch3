package org.fastcatsearch.statistics;

import org.fastcatsearch.statistics.log.SearchLog;
import org.fastcatsearch.statistics.util.LogParser;

/**
 * 검색엔진이 남긴 로그파일을 한줄씩 읽어들인다.
 * 검색키워드는 모두 lowercase로 변환하여 대소문자를 구분하지 않도록 한다.
 * */
public class SearchLogParser extends LogParser<SearchLog> {

	public SearchLog parseLine(String line) {
		
		String[] el = line.split("\t");
		String keyword = null;
		String prevKeyword = null;
		if(el.length > 0){
			keyword = el[0].trim().toLowerCase();
			if(el.length > 1){
				prevKeyword = el[1].trim().toLowerCase();
			}
			return new SearchLog(keyword, prevKeyword);
		}
		
		return null;
	}
}
