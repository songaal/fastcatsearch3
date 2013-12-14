package org.fastcatsearch.statistics.util;

public abstract class RunEntryReader<E extends RunEntry> {
	
	public abstract E read();
	
	public void close(){
		
	}
	
}
