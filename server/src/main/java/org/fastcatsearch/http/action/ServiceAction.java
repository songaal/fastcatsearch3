package org.fastcatsearch.http.action;

import java.io.Writer;

import org.fastcatsearch.util.JSONPResultWriter;
import org.fastcatsearch.util.JSONResultWriter;
import org.fastcatsearch.util.ResultWriter;
import org.fastcatsearch.util.XMLResultWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public abstract class ServiceAction extends HttpAction {
	private static final String DEFAULT_ROOT_ELEMENT = "fastcatsearch";
	public static final String DEFAULT_CHARSET = "utf-8";
	public static enum Type { json, xml, jsonp, html };
	protected Type resultType;
	
	public ServiceAction(String type){
		this.resultType = detectType(type);
	}
	protected Type detectType(String typeStr) {
		if(typeStr != null){
			if(typeStr.equalsIgnoreCase(Type.json.name())){
				return Type.json;
			}else if(typeStr.equalsIgnoreCase(Type.xml.name())){
				return Type.xml;
			}else if(typeStr.equalsIgnoreCase(Type.jsonp.name())){
				return Type.jsonp;
			}else if(typeStr.equalsIgnoreCase(Type.html.name())){
				return Type.html;
			}
		}
		return Type.json;
	}
	
	protected void writeHeader(ActionResponse response) {
		writeHeader(response, DEFAULT_CHARSET);
	}
	protected void writeHeader(ActionResponse response, String responseCharset) {
		response.setStatus(HttpResponseStatus.OK);
		if (resultType == Type.json) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (resultType == Type.jsonp) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (resultType == Type.xml) {
			response.setContentType("text/xml; charset=" + responseCharset);
		} else if (resultType == Type.html) {
			response.setContentType("text/html; charset=" + responseCharset);
		} else {
			response.setContentType("application/json; charset=" + responseCharset);
		}
	}
	protected ResultWriter getDefaultResultWriter(Writer writer){
		return getResultWriter(writer, DEFAULT_ROOT_ELEMENT, true, null);
	}
	protected ResultWriter getResultWriter(Writer writer, String rootElement, boolean isBeautify, String jsonCallback) {
		ResultWriter resultWriter = null;
		if (resultType == Type.json) {
			resultWriter = new JSONResultWriter(writer, isBeautify);
		} else if (resultType == Type.jsonp) {
			resultWriter = new JSONPResultWriter(writer, jsonCallback, isBeautify);
		} else if (resultType == Type.xml) {
			resultWriter = new XMLResultWriter(writer, rootElement, isBeautify);
		}
		return resultWriter;
	}

}
