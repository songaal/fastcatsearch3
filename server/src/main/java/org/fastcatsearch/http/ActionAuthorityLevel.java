package org.fastcatsearch.http;

/**
 * */
public enum ActionAuthorityLevel {
	NONE, READABLE, WRITABLE;
	
	public boolean isLargerThan(ActionAuthorityLevel level){
		if(level == null) {
			return false;
		}
		if(level == WRITABLE){
			return this == WRITABLE;
		} else if(level == READABLE){
			return this == WRITABLE || this == READABLE;
		} else if(this == NONE) {
			return this == level;
		}
		return this != NONE;
	}
}
