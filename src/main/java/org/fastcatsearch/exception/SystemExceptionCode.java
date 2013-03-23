package org.fastcatsearch.exception;

public abstract class SystemExceptionCode {
	public static int CATEGORY_INDEX = 1;
	public static int CATEGORY_SEARCH = 1;
	
	int category;
	int code;
	String message;
	
	public SystemExceptionCode(int category, int code, String message){
		this.category = category;
		this.code = code;
		this.message = message;
	}
	
	public String toString(){
		return "["+category+"-"+code+"]"+message;
	}
			
}
