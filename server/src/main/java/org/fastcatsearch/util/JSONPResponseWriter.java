package org.fastcatsearch.util;

import java.io.IOException;
import java.io.Writer;

public class JSONPResponseWriter extends JSONResponseWriter {
	public final static String DEFAULT_CALLBACK = "__callback";
	public String callback;
	private Writer w;

	public JSONPResponseWriter(Writer w, String callback) {
		this(w, callback, false, false, false);
	}

	public JSONPResponseWriter(Writer w, String callback, boolean beautify, boolean isKeyLowercase, boolean noUnicode) {
		super(w, beautify, isKeyLowercase, noUnicode);
		this.w = w;
		if(callback == null){
			callback = DEFAULT_CALLBACK;
		}
		this.callback = callback;
		try {
			w.write(callback);
			w.write("(");
			if (super.isBeautify()) {
				w.write("\r\n");
			}
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	public void done() {
		try {
			if (super.isBeautify()) {
				w.write("\r\n");
			}
			w.write(")");
		} catch (IOException e) {
			logger.error("", e);
		}
		
		super.done();
	}
}
