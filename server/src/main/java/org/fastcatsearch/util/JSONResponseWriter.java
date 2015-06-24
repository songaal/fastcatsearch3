package org.fastcatsearch.util;

import org.json.JSONException;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;

public class JSONResponseWriter implements ResponseWriter {
	protected static Logger logger = LoggerFactory.getLogger(JSONResponseWriter.class);
	protected Writer w;
	protected CustomJSONWriter writer;
	protected boolean beautify;
	protected boolean isKeyLowercase;
    /*
    * “ 와 같은 특수문자를 \u201c 와 같이 유니코드로 변환하는지 여부.
    * 디폴트로는 true
    * 검색 client에서 검색결과를 매번 unicode를 unescape하는 것이 번거롭다면, JSONResponseWriter 생성시 noUnicode=true 옵션을 사용하여 유니코드 변환을 막는다.
    * */
    protected boolean escapeToUnicode;

	public JSONResponseWriter(Writer w) {
		this(w, false, false, false);
	}
	
	public JSONResponseWriter(Writer w, boolean beautify, boolean isKeyLowercase, boolean noUnicode) {
		this.w = w;
		writer = new CustomJSONWriter(w);
		this.beautify = beautify;
        this.isKeyLowercase = isKeyLowercase;
        this.escapeToUnicode = !noUnicode;
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
            if(isKeyLowercase) {
                key = key.toLowerCase();
            }
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
				writer.value(obj, escapeToUnicode);
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