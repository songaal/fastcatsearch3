package org.fastcatsearch.statistics.util;

public abstract class RunEntry implements Comparable {

	protected String rawLine;
	
	public RunEntry(String rawLine){
		this.rawLine = rawLine;
	}
	public String getRawLine(){
		return rawLine;
	}
	
	@Override
	public String toString(){
		return rawLine;
	}
	
	public abstract void merge(RunEntry other);
	
}
