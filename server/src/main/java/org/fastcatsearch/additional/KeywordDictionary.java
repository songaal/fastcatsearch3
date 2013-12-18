package org.fastcatsearch.additional;

import org.fastcatsearch.ir.dictionary.ReadableDictionary;
import org.fastcatsearch.ir.dictionary.WritableDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface KeywordDictionary extends ReadableDictionary, WritableDictionary {
	public final static Logger logger = LoggerFactory.getLogger(KeywordDictionary.class);
	
	public static enum KeywordDictionaryType { 
		POPULAR_KEYWORD_REALTIME, POPULAR_KEYWORD_DAY, POPULAR_KEYWORD_WEEK //인기검색어 실시간, 이전일, 이전주  
		, RELATE_KEYWORD
		, KEYWORD_SUGGESTION //검색어추천(자동완성)
		, AD_KEYWORD //광고키워드
	};
}
