package org.fastcatsearch.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class ResultWriter implements ResultStringer {

	ResultStringer stringer;
	HttpServletResponse response;
	String charset;
	
	public ResultWriter(HttpServletResponse response, String charset, ResultStringer stringer) {
		this.response = response;
		this.charset = charset;
		this.stringer = stringer;
	}

	@Override
	public ResultStringer object() throws StringifyException {
		stringer.object();
		return this;
	}

	@Override
	public ResultStringer endObject() throws StringifyException {
		stringer.endObject();
		return this;
	}

	@Override
	public ResultStringer array(String arrayName) throws StringifyException {
		stringer.array(arrayName);
		return this;
	}

	@Override
	public ResultStringer endArray() throws StringifyException {
		stringer.endArray();
		return this;
	}

	@Override
	public ResultStringer key(String key) throws StringifyException {
		stringer.key(key);
		return this;
	}

	@Override
	public ResultStringer value(Object obj) throws StringifyException {
		stringer.value(obj);
		return this;
	}
	
	public void write() {
		try {
			if(stringer instanceof JSONResultStringer) {
	    		response.setContentType("application/json; charset="+charset);
			} else if(stringer instanceof JSONPResultStringer) {
	    		response.setContentType("application/json; charset="+charset);
			} else if(stringer instanceof XMLResultStringer) {
	    		response.setContentType("text/xml; charset="+charset);
	//    	}else if(resultType == IS_ALIVE){
	//    		response.setContentType("text/html; charset="+responseCharset);
	//    		writer.write("FastCat/OK\n<br/>" + new Date());
			}
			PrintWriter writer = response.getWriter();
			writer.write(stringer.toString());
		} catch (IOException e) {
		} finally {
		}
	}
}
