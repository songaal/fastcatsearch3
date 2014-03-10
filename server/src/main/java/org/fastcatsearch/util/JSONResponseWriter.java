package org.fastcatsearch.util;

import java.io.IOException;
import java.io.Writer;

import org.json.JSONException;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONResponseWriter implements ResponseWriter {
	protected static Logger logger = LoggerFactory.getLogger(JSONResponseWriter.class);
	protected Writer w;
	protected JSONWriter writer;
	protected boolean beautify;
	
	public JSONResponseWriter(Writer w) {
		this(w, false);
	}
	
	public JSONResponseWriter(Writer w, boolean beautify) {
		this.w = w;
		writer = new JSONWriter(w);
		this.beautify = beautify;
	}
	
	public boolean isBeautify() {
		return beautify;
	}

	@Override
	public ResponseWriter object() throws ResultWriterException {
		try {
			writer.object();
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public ResponseWriter endObject() throws ResultWriterException {
		try {
			writer.endObject();
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public ResponseWriter array() throws ResultWriterException {
		return array(null);
	}
	
	@Override
	public ResponseWriter array(String arrayName) throws ResultWriterException {
		try {
			writer.array();
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public ResponseWriter endArray() throws ResultWriterException {
		try {
			writer.endArray();
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public ResponseWriter key(String key) throws ResultWriterException {
		try {
			writer.key(key);
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public ResponseWriter value(Object obj) throws ResultWriterException {
		try {
			if(obj == null){
				writer.value("");
			}else{
				writer.value(obj);
			}
			return this;
		} catch (JSONException e) {
			throw new ResultWriterException(e);
		}
	}

	@Override
	public void done() {
		if(w != null){
			try {
				w.flush();
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