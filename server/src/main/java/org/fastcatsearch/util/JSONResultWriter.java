package org.fastcatsearch.util;

import java.io.IOException;
import java.io.Writer;

import org.json.JSONException;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONResultWriter implements ResultWriter {
	protected static Logger logger = LoggerFactory.getLogger(JSONResultWriter.class);
	protected Writer w;
	protected JSONWriter writer;
	protected boolean beautify;
	
	public JSONResultWriter(Writer w) {
		this(w, false);
	}
	
	public JSONResultWriter(Writer w, boolean beautify) {
		this.w = w;
		writer = new JSONWriter(w);
		this.beautify = beautify;
	}
	
	public boolean isBeautify() {
		return beautify;
	}

	@Override
	public ResultWriter object() throws ResultWriterException {
		try {
			writer.object();
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public ResultWriter endObject() throws ResultWriterException {
		try {
			writer.endObject();
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public ResultWriter array(String arrayName) throws ResultWriterException {
		try {
			writer.array();
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public ResultWriter endArray() throws ResultWriterException {
		try {
			writer.endArray();
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public ResultWriter key(String key) throws ResultWriterException {
		try {
			writer.key(key);
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public ResultWriter value(Object obj) throws ResultWriterException {
		try {
			writer.value(obj);
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public void done() {
		if(w != null){
			try {
				w.close();
			} catch (IOException e) {
				logger.error("close error", e);
			}
		}
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