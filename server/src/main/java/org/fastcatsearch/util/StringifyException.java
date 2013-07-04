package org.fastcatsearch.util;

import org.json.JSONException;

public class StringifyException extends JSONException {

	private static final long serialVersionUID = 3485971749247649163L;
	
	public StringifyException(Throwable e) {
		super(e);
	}
}
