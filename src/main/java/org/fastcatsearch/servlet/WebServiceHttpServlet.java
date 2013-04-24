package org.fastcatsearch.servlet;

import javax.servlet.http.HttpServlet;

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
		String type = getServletConfig().getInitParameter("result_format");
		if(type != null){
			if(type.equalsIgnoreCase("json")){
				resultType = JSON_TYPE;
			}else if(type.equalsIgnoreCase("xml")){
				resultType = XML_TYPE;
			}else if(type.equalsIgnoreCase("jsonp")){
				resultType = JSONP_TYPE;
			}
		}
	}
}
