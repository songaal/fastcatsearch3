package org.fastcatsearch.http.service.action;

import java.io.Writer;

import org.fastcatsearch.util.JSONPResultWriter;
import org.fastcatsearch.util.JSONResultWriter;
import org.fastcatsearch.util.ResultWriter;
import org.fastcatsearch.util.XMLResultWriter;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public abstract class ServiceAction extends HttpAction {
	
	public static final String DEFAULT_CHARSET = "utf-8";
	public static enum Type { json, xml, jsonp };
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
		}
	}
	
	protected ResultWriter getResultWriter(Writer writer, String rootElement, boolean isBeautify, String jsonCallback) {
		ResultWriter rStringer = null;
		if (resultType == Type.json) {
			rStringer = new JSONResultWriter(writer, isBeautify);
		} else if (resultType == Type.jsonp) {
			rStringer = new JSONPResultWriter(writer, jsonCallback, isBeautify);
		} else if (resultType == Type.xml) {
			rStringer = new XMLResultWriter(writer, rootElement, isBeautify);
		}
		return rStringer;
	}

}
