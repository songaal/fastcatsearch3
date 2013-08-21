package org.fastcatsearch.util;

import java.io.Writer;

import org.json.JSONException;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONResultWriter implements ResultWriter {
	protected static Logger logger = LoggerFactory.getLogger(JSONResultWriter.class);
	
	protected JSONWriter writer;
	protected boolean beautify;
	
	public JSONResultWriter(Writer w) {
		this(w, false);
	}
	
	public JSONResultWriter(Writer w, boolean beautify) {
		writer = new JSONWriter(w);
		this.beautify = beautify;
	}
	
	public boolean isBeautify() {
		return beautify;
	}

	@Override
	public ResultWriter object() throws StringifyException {
		try {
			writer.object();
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public ResultWriter endObject() throws StringifyException {
		try {
			writer.endObject();
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public ResultWriter array(String arrayName) throws StringifyException {
		try {
			writer.array();
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public ResultWriter endArray() throws StringifyException {
		try {
			writer.endArray();
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public ResultWriter key(String key) throws StringifyException {
		try {
			writer.key(key);
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public ResultWriter value(Object obj) throws StringifyException {
		try {
			writer.value(obj);
			return this;
		} catch (JSONException e) {
			throw new StringifyException(e);
		}
	}

	@Override
	public void done() {
		
	}
	
//	public String toString() {
//		if(beautify) {
//			try {
//				JSONObject obj = new JSONObject(writer.toString());
//				return obj.toString(2);
//			} catch (JSONException e) {
//			}
//		} else {
//			return writer.toString();
//		}
//		return null;
//	}
	
}