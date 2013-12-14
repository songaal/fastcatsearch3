package org.fastcatsearch.statistics.util;

/**
 * 일부 합산된 RUN 로그파일을 읽어들여 한줄씩 파싱하여 RunEntry로 리턴한다.
 * */
public abstract class RunEntryReader<E extends RunEntry> {
	
	public abstract boolean read();
	
	public abstract E entry();
	
	public abstract void close();
	
}
