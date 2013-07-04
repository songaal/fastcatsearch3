package org.fastcatsearch.util;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

public class JSONResultStringer implements ResultStringer {
	
	JSONStringer stringer;
	boolean beautify;
	
	public JSONResultStringer() {
		this(false);
	}
	
	public JSONResultStringer(boolean beautify) {
		stringer = new JSONStringer();
		this.beautify = beautify;
	}
	
	public boolean isBeautify() {
		return beautify;
	}

	@Override
	public ResultStringer object() throws StringifyException {
		try {
			stringer.object();
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public ResultStringer endObject() throws StringifyException {
		try {
			stringer.endObject();
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public ResultStringer array(String arrayName) throws StringifyException {
		try {
			stringer.array();
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public ResultStringer endArray() throws StringifyException {
		try {
			stringer.endArray();
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public ResultStringer key(String key) throws StringifyException {
		try {
			stringer.key(key);
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public ResultStringer value(Object obj) throws StringifyException {
		try {
			stringer.value(obj);
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}
	
	@Override
	public String toString() {
		if(beautify) {
			try {
				JSONObject obj = new JSONObject(stringer.toString());
				return obj.toString(2);
			} catch (JSONException e) {
			}
		} else {
			return stringer.toString();
		}
		return null;
	}
}