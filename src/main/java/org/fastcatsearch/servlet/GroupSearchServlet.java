/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.servlet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.common.Strings;
import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.ir.config.FieldSetting;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.Schema;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.field.ScoreField;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.io.AsciiCharTrie;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.ir.util.Formatter;
import org.fastcatsearch.job.GroupSearchJob;
import org.fastcatsearch.job.SearchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupSearchServlet extends JobHttpServlet {
	
	private static Logger searchLogger = LoggerFactory.getLogger("SEARCH_LOG");
	private static AtomicLong taskSeq = new AtomicLong();
	
    public GroupSearchServlet(int resultType){
    	super(resultType);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	@SuppressWarnings("rawtypes")
		Enumeration enumeration = request.getParameterNames();
    	String timeoutStr = request.getParameter("timeout");
    	String isAdmin = request.getParameter("admin");
    	String collectionName = request.getParameter("cn");
    	String requestCharset = request.getParameter("requestCharset");
    	String responseCharset = request.getParameter("responseCharset");
    	String userData = request.getParameter("ud");
    	StringBuffer sb = new StringBuffer();
    	while(enumeration.hasMoreElements()){
    		String key = (String) enumeration.nextElement();
    		String value = request.getParameter(key);
    		sb.append(key);
    		sb.append("=");
    		sb.append(value);
    		sb.append("&");
    	}
    	doReal(sb.toString(), timeoutStr, isAdmin, collectionName, userData, request, response, requestCharset, responseCharset);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String queryString = request.getQueryString();
    	String timeoutStr = request.getParameter("timeout");
    	String isAdmin = request.getParameter("admin");
    	String collectionName = request.getParameter("cn");
    	String requestCharset = request.getParameter("requestCharset");
    	String responseCharset = request.getParameter("responseCharset");
    	String userData = request.getParameter("ud");
    	doReal(queryString, timeoutStr, isAdmin, collectionName, userData, request, response, requestCharset, responseCharset);
    	
    }
    
    private void doReal(String queryString, String timeoutStr, String isAdmin, String collectionName, String userData, HttpServletRequest request, HttpServletResponse response, String requestCharset, String responseCharset) throws ServletException, IOException {
    	if(requestCharset == null)
    		requestCharset = "UTF-8";
    	
    	if(responseCharset == null)
    		responseCharset = "UTF-8";
    	
//    	logger.debug("requestCharset = "+requestCharset);
//    	logger.debug("responseCharset = "+responseCharset);
    	if(queryString != null){
    		queryString = URLDecoder.decode(queryString, requestCharset);
    		if(queryString.endsWith("&")) { 
    			queryString = queryString.substring(0,queryString.length()-1); 
    		}
    	}
    	logger.debug("queryString = "+queryString);
    	
    	//TODO 디폴트 시간을 셋팅으로 빼자.
    	int timeout = 5;
    	if(timeoutStr != null)
    		timeout = Integer.parseInt(timeoutStr);
    	logger.debug("timeout = "+timeout+" s");
    	
    	long seq = taskSeq.incrementAndGet();
		searchLogger.info(seq+", "+queryString);
		
		
		response.setCharacterEncoding(responseCharset);
    	response.setStatus(HttpServletResponse.SC_OK);
    	
    	PrintWriter w = response.getWriter();
    	BufferedWriter writer = new BufferedWriter(w);
    	
    	if(resultType == JSON_TYPE){
    		response.setContentType("application/json; charset="+responseCharset);
    	}else if(resultType == XML_TYPE){
    		response.setContentType("text/xml; charset="+responseCharset);
    	}else if(resultType == JSONP_TYPE){
    		response.setContentType("application/json; charset="+responseCharset);
    	}
    	
    	
    	if(resultType == JSONP_TYPE) {
    		String callback = request.getParameter("jsoncallback");
    		writer.write(callback+"(");
    	}
    	
    	long searchTime = 0;
    	long st = System.currentTimeMillis();
    	
    	GroupSearchJob job = new GroupSearchJob();
    	job.setArgs(new String[]{queryString});
    	
    	GroupResults result = null;
    	
		ResultFuture jobResult = JobService.getInstance().offer(job);
		Object obj = jobResult.poll(timeout);
		searchTime = (System.currentTimeMillis() - st);
		if(jobResult.isSuccess()){
			result = (GroupResults)obj;
		}else{
			String errorMsg = "";
			if(obj instanceof Throwable){
				errorMsg = ((Throwable)obj).getMessage();
			}
			searchLogger.info(seq+", -1, "+errorMsg);
			
			if(resultType == JSON_TYPE){
				if(errorMsg != null){
					errorMsg = Formatter.escapeJSon(errorMsg);
				}
				writer.write("{");
				writer.newLine();
	    		writer.write("\t\"status\": \"1\",");
	    		writer.newLine();
	    		writer.write("\t\"time\": \""+Formatter.getFormatTime(searchTime)+"\",");
	    		writer.newLine();
	    		writer.write("\t\"total_count\": \"0\",");
	    		writer.newLine();
	    		writer.write("\t\"error_msg\": \""+errorMsg+"\"");
	    		writer.newLine();
	    		writer.write("}");
			}else if(resultType == XML_TYPE){
				if(errorMsg != null){
					errorMsg = Formatter.escapeXml(errorMsg);
				}
				writer.write("<fastcat>");
				writer.newLine();
	    		writer.write("\t<status>1</status>");
	    		writer.newLine();
	    		writer.write("\t<total_count>0</total_count>");
	    		writer.newLine();
	    		writer.write("\t<time>"+Formatter.getFormatTime(searchTime)+"</time>");
	    		writer.newLine();
	    		writer.write("\t<error_msg>"+errorMsg+"</error_msg>");
	    		writer.newLine();
	    		writer.write("</fastcat>");
			}
			
			writer.close();
    		return;
		}
		
    	//SUCCESS
		String logStr = searchTime+", "+result.totalSearchCount();
		if(result != null){
			String grStr = ", [";
			GroupResult[] gr = result.groupResultList();
			for (int i = 0; i < gr.length; i++) {
				if(i > 0)
					grStr += ", ";
				grStr += gr[i].size();
			}
			grStr += "]";
			logStr += grStr;
		}
		searchLogger.info(seq+", "+logStr);
		
		if(resultType == JSON_TYPE || resultType == JSONP_TYPE){
			//JSON
			writer.write("{");
			writer.newLine();
			writer.write("\t\"status\": \"0\",");
			writer.newLine();
			writer.write("\t\"time\": \""+Strings.getHumanReadableTimeInterval(searchTime)+"\",");
			writer.newLine();
			writer.write("\t\"total_count\": \""+result.totalSearchCount()+"\",");
			writer.newLine();
			
    		//group
    		writer.write("\t\"group_result\":");
//	    		AggregationResult aggregationResult = result.getGroupResult();
			GroupResult[] groupResultList = result.groupResultList();
    		if(groupResultList == null){
    			writer.write(" []");
    		}else{
        		writer.write("\t[");
        		for (int i = 0; i < groupResultList.length; i++) {
        			writer.write("\t\t[");
					GroupResult groupResult = groupResultList[i];
					int size = groupResult.size();
					for (int k = 0; k < size; k++) {
						GroupEntry e = groupResult.getEntry(k);
						String keyData = e.key.getKeyString();
						String functionName = groupResult.functionName();
						keyData = Formatter.escapeJSon(keyData);
						
						writer.write("\t\t{\"_no_\": \"");
						writer.write((k+1)+"");
						writer.write("\", \"key\": \"");
						writer.write(keyData);
						writer.write("\", \"freq\": \"");
						writer.write(e.count()+"");
						if(groupResult.hasAggregationData()){
							String r = e.getGroupingObjectResultString();
							if(r == null){
								r = "";
							}
							writer.write("\", \""+functionName+"\": \"");
							writer.write(r);
						}
						writer.write("\"}");
						if(k < size - 1)
							writer.write(",");
						else
							writer.write("");
						
					}
					writer.write("\t\t]");
					if(i < groupResultList.length - 1)
						writer.write(",");
					else
						writer.write("");
					
				}
        		
        		writer.write("\t]");
    		}//if else
			writer.write("}");
		}else if(resultType == XML_TYPE){
			//XML
			//this does not support admin test, have no column meta data
			
			writer.write("<fastcat>");
			writer.newLine();
			writer.write("\t<status>0</status>");
			writer.newLine();
			writer.write("\t<time>");
			writer.write(Formatter.getFormatTime(searchTime));
			writer.write("</time>");
			writer.newLine();
			writer.write("\t<total_count>");
			writer.write(result.totalSearchCount()+"");
			writer.write("</total_count>");
			writer.newLine();
			
	    		//group
			GroupResult[] groupResultList = result.groupResultList();
    		if(groupResultList == null){
    			writer.write("\t<group_result />");
    			writer.newLine();
    		}else{
    			writer.write("\t<group_result>");
    			writer.newLine();
        		for (int i = 0; i < groupResultList.length; i++) {
        			writer.write("\t\t<group_list>");
        			writer.newLine();
					GroupResult groupResult = groupResultList[i];
					int size = groupResult.size();
					for (int k = 0; k < size; k++) {
						writer.write("\t\t\t<group_item>");
						writer.newLine();
						GroupEntry e = groupResult.getEntry(k);
						String keyData = e.key.getKeyString();
						keyData = Formatter.escapeXml(keyData);
						String functionName = groupResult.functionName();
						
						writer.write("\t\t\t\t<_no_>");
						writer.write((k+1)+"");
						writer.write("</_no_>");
						writer.newLine();
						writer.write("\t\t\t\t<key>");
						writer.write(keyData);
						writer.write("</key>");
						writer.newLine();
						writer.write("\t\t\t\t<freq>");
						writer.write(e.count()+"");
						writer.write("</freq>");
						writer.newLine();
						if(groupResult.hasAggregationData()){
							String r = e.getGroupingObjectResultString();
							if(r == null){
								r = "";
							}
							writer.write("\t\t\t\t<"+functionName+">");
							writer.write(r);
							writer.write("</"+functionName+">");
							writer.newLine();
						}
						writer.write("\t\t\t</group_item>");
						writer.newLine();
						
					}
					writer.write("\t\t</group_list>");
					writer.newLine();
				}
        		writer.write("\t</group_result>");
    			writer.newLine();
	    			
	    	}//if else
			writer.write("</fastcat>");
		}
		
    	if(resultType == JSONP_TYPE) {
    		writer.write(");");
    	}

    	writer.close();
    }
}
