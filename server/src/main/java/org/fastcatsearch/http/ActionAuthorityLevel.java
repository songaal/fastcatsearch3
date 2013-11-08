package org.fastcatsearch.http;

/**
 * */
public enum ActionAuthorityLevel {
	NONE, READABLE, WRITABLE;
	
	public boolean isLargerThan(ActionAuthorityLevel level){
		if(level == WRITABLE){
			return this == WRITABLE;
		}else if(level == READABLE){
			return this == WRITABLE || this == READABLE;
		}
		return this != NONE;
	}
}
