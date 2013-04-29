package org.fastcatsearch.util;

public class JSONPResultStringer extends JSONResultStringer {
	public String callback;
	
	public JSONPResultStringer(String callback) {
		this(callback,false);
	}
	
	public JSONPResultStringer(String callback, boolean beautify) {
		super(beautify);
		this.callback = callback;
	}
	
	@Override
	public String toString() {
		if(super.isBeautify()) {
			return callback+"(\r\n"+super.toString()+"\r\n)";
		} else {
			return callback+"("+super.toString()+")";
		}
	}
}
