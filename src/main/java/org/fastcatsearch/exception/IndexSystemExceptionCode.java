package org.fastcatsearch.exception;

public class IndexSystemExceptionCode {
	private static int CATEGORY_NUM = SystemExceptionCode.CATEGORY_INDEX;
	private static int CODE_NUM = 1;
	public static SystemExceptionCode UnknownSourceException = new SystemExceptionCode(CATEGORY_NUM, CODE_NUM++
			, "무슨무슨 에러입니다."){};
}
