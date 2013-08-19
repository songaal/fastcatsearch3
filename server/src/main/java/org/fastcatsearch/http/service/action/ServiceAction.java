package org.fastcatsearch.http.service.action;

import org.fastcatsearch.util.JSONPResultStringer;
import org.fastcatsearch.util.JSONResultStringer;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.XMLResultStringer;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public abstract class ServiceAction extends HttpAction {

	public static final int JSON_TYPE = 0;
	public static final int XML_TYPE = 1;
	public static final int JSONP_TYPE = 2;
	
	protected String responseCharset;
	protected int resultType;
	
	public ServiceAction(){
	}
	
	public ServiceAction(String type){
		int resultType = detectType(type);
		if (resultType != -1) {
			this.resultType = resultType;
		}else{
			this.resultType = JSON_TYPE;
		}
	}
	protected int detectType(String typeStr) {
		if(typeStr != null){
			if(typeStr.equalsIgnoreCase("json")){
				return JSON_TYPE;
			}else if(typeStr.equalsIgnoreCase("xml")){
				return XML_TYPE;
			}else if(typeStr.equalsIgnoreCase("jsonp")){
				return JSONP_TYPE;
			}
		}
		return -1;
	}
	
	protected void writeHeader(ActionResponse response, String responseCharset) {
		response.setStatus(HttpResponseStatus.OK);
		if (resultType == JSON_TYPE) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (resultType == JSONP_TYPE) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (resultType == XML_TYPE) {
			response.setContentType("text/xml; charset=" + responseCharset);
		}
	}
	
	protected ResultStringer getResultStringer(String rootElement, boolean isBeautify, String jsonCallback) {
		ResultStringer rStringer = null;
		if (resultType == JSON_TYPE) {
			rStringer = new JSONResultStringer(isBeautify);
		} else if (resultType == JSONP_TYPE) {
			rStringer = new JSONPResultStringer(jsonCallback,isBeautify);
		} else if (resultType == XML_TYPE) {
			rStringer = new XMLResultStringer(rootElement, isBeautify);
		}
		return rStringer;
	}

}
