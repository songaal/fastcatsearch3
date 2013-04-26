package org.fastcatsearch.util;

public class JSONPResultStringer extends JSONResultStringer {
	public String callback;
	
	public JSONPResultStringer(String callback) {
		this.callback = callback;
	}
	@Override
	public String toString() {
		return callback+"("+super.toString()+")";
	}
}
