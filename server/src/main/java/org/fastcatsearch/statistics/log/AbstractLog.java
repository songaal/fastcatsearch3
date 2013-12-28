package org.fastcatsearch.statistics.log;

public abstract class AbstractLog {

	private String key;
	
	public AbstractLog(String key){
		this.key = key;
	}
	
	public String getKey(){
		return key;
	}
}
