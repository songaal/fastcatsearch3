package org.fastcatsearch.http;

/**
 * 여기에 action 권한들을 정리한다.
 * */
public enum ActionAuthority {
	NULL("Null"),
	Dictionary ("Dictionary"), 
	Collections ("Collections"), 
	Analysis  ("Analysis"),
	Servers ("Servers"),
	Logs ("Logs"),
	Settings ("Settings");
	
	private String name;
	
	private ActionAuthority(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}