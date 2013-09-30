package org.fastcatsearch.servlet;

import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.util.JSONPResponseWriter;
import org.fastcatsearch.util.JSONResponseWriter;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.XMLResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServiceHttpServlet extends HttpServlet {

	private static final long serialVersionUID = -6799888063493417231L;
	public static final int JSON_TYPE = 0;
	public static final int XML_TYPE = 1;
	public static final int JSONP_TYPE = 2;
	
	protected static Logger logger = LoggerFactory.getLogger(WebServiceHttpServlet.class);
	
	protected int resultType = JSON_TYPE;
	
	public WebServiceHttpServlet(){ }
	
	public WebServiceHttpServlet(int resultType){
    	this.resultType = resultType;
    }
	
	@Override
	public void init(){
		String typeStr = getServletConfig().getInitParameter("result_format");
		int resultType = detectType(typeStr);
		if(resultType!=-1) {
			this.resultType = resultType;
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
	
	protected String getParameter(HttpServletRequest request, String key, String defaultValue) {
		String value = request.getParameter(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
	protected String getParameter(Map<String,String> kvmap, String key, String defaultValue) {
		String value = kvmap.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}
	
	protected String getParameterFromMap(Map<String,String[]> kvmap, String key, String defaultValue) {
		String[] value = kvmap.get(key);
		if (value != null && value.length > 0) {
			return value[0];
		}
		return defaultValue;
	}
	
	protected ResponseWriter getResultStringer(String rootElement, boolean isBeautify, String jsonCallback) {
		ResponseWriter rStringer = null;
//		if (resultType == JSON_TYPE) {
//			rStringer = new JSONResultWriter(isBeautify);
//		} else if (resultType == JSONP_TYPE) {
//			rStringer = new JSONPResultWriter(jsonCallback,isBeautify);
//		} else if (resultType == XML_TYPE) {
//			rStringer = new XMLResultWriter(rootElement, isBeautify);
//		}
		return rStringer;
	}
	
	protected void writeHeader(HttpServletResponse response, ResponseWriter stringer, String responseCharset) {
		response.reset();
		response.setStatus(HttpServletResponse.SC_OK);
		if (stringer instanceof JSONResponseWriter) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (stringer instanceof JSONPResponseWriter) {
			response.setContentType("application/json; charset=" + responseCharset);
		} else if (stringer instanceof XMLResponseWriter) {
			response.setContentType("text/xml; charset=" + responseCharset);
		}
	}
	
	protected String[] getURIArray(HttpServletRequest req) {
		return req.getRequestURI().split("/");
	}
}
