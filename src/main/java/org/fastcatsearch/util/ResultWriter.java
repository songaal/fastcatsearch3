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
		this.stringer = stringer;
	}

	@Override
	public ResultStringer object() throws StringifyException {
		return stringer.object();
	}

	@Override
	public ResultStringer endObject() throws StringifyException {
		return stringer.endObject();
	}

	@Override
	public ResultStringer array(String arrayName) throws StringifyException {
		return stringer.array(arrayName);
	}

	@Override
	public ResultStringer endArray() throws StringifyException {
		return stringer.endArray();
	}

	@Override
	public ResultStringer key(String key) throws StringifyException {
		return stringer.key(key);
	}

	@Override
	public ResultStringer value(Object obj) throws StringifyException {
		return stringer.value(obj);
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
