package org.fastcatsearch.util;

/*
 * 이상한 단어를 포함한 키워드는 제거한다.
 * 인기검색어등 서비스화면에 사용되는 모듈에서 사용될수 있다.
 * */
public class KeywordFilter {
	public KeywordFilter() {

	}

	// 필터링에 통과하면 true, 걸리면 false
	public boolean filtering(String source) {
		if (source == null || source.length() == 0) {
			return false;
		}

		source = source.trim();

		if (source.length() == 0) {
			return false;
		}

		for (int i = 0; i < source.length(); i++) {
			char ch = source.charAt(i);
			if (isStrangeHangul(ch)) {
				return false;
			}
		}

		return true;
	}

	private boolean isStrangeHangul(char ch) {
		return ch >= 'ㄱ' && ch <= 'ㅎ';
	}

}
