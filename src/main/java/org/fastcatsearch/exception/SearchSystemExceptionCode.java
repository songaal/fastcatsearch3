package org.fastcatsearch.exception;

public class SearchSystemExceptionCode {
	private static int CATEGORY_NUM = SystemExceptionCode.CATEGORY_SEARCH;
	private static int CODE_NUM = 1;
	public static SystemExceptionCode NoKeywordException = new SystemExceptionCode(CATEGORY_NUM, CODE_NUM++
			, "무슨무슨 에러입니다."){};
}
