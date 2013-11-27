package org.fastcatsearch.http.action;

import java.io.Writer;

import org.fastcatsearch.util.JSONPResponseWriter;
import org.fastcatsearch.util.JSONResponseWriter;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.XMLResponseWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public abstract class ServiceAction extends HttpAction {
	public static final String DEFAULT_ROOT_ELEMENT = "response";
	public static final String DEFAULT_CHARSET = "utf-8";
	public static enum Type { json, xml, jsonp, html, text };
	
	public ServiceAction(){ 
	}
	
	protected void writeHeader(ActionResponse response) {
		writeHeader(response, DEFAULT_CHARSET);
	}
	protected void writeHeader(ActionResponse response, String responseCharset) {
		response.setStatus(HttpResponseStatus.OK);
		logger.debug("resultType > {}",resultType);
		if (resultType == Type.json) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (resultType == Type.jsonp) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (resultType == Type.xml) {
			response.setContentType("text/xml; charset=" + responseCharset);
		} else if (resultType == Type.html) {
			response.setContentType("text/html; charset=" + responseCharset);
		} else if (resultType == Type.text) {
			response.setContentType("text/plain; charset=" + responseCharset);
		} else {
			response.setContentType("application/json; charset=" + responseCharset);
		}
	}
	protected ResponseWriter getDefaultResponseWriter(Writer writer){
		return getResponseWriter(writer, DEFAULT_ROOT_ELEMENT, true, null);
	}
	
	protected ResponseWriter getResponseWriter(Writer writer, String rootElement, boolean isBeautify, String jsonCallback) {
		ResponseWriter resultWriter = null;
		if (resultType == Type.json) {
			resultWriter = new JSONResponseWriter(writer, isBeautify);
		} else if (resultType == Type.jsonp) {
			resultWriter = new JSONPResponseWriter(writer, jsonCallback, isBeautify);
		} else if (resultType == Type.xml) {
			resultWriter = new XMLResponseWriter(writer, rootElement, isBeautify);
		}
		return resultWriter;
	}
}
