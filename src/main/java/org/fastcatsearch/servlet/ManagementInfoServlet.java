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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.management.JvmCpuInfo;
import org.fastcatsearch.management.JvmMemoryInfo;
import org.fastcatsearch.management.ManagementInfoService;
import org.json.JSONException;
import org.json.JSONStringer;


/**
 * 데이터 갱신은 ManagementInfoService 에서 주기적으로 수행하며, 이 servlet은 데이터만 가져가게됨.
 * @author swsong
 *
 */
public class ManagementInfoServlet extends WebServiceHttpServlet {
	
	private static final long serialVersionUID = 963640595944747847L;
	private ManagementInfoService service = ManagementInfoService.getInstance();
	
    public ManagementInfoServlet(int resultType){
    	super(resultType);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
    Random r = new Random(System.currentTimeMillis());
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	if(!service.isRunning()){
    		throw new ServletException(service.getClass().getName()+"이 시작중이 아닙니다.");
    	}
    	
    	String q = request.getParameter("q");
    	String simple = request.getParameter("simple");
    	String test = request.getParameter("test");
    	boolean isTest = false;
    	if(test != null && test.equals("true")){
    		isTest = true;
    	}
    	
    	response.setCharacterEncoding("utf-8");
    	PrintWriter w = response.getWriter();
    	if(q != null){
    		//지원사항을 알려준다.
        	response.setStatus(HttpServletResponse.SC_OK);
        	response.setContentType("application/json;");
        	
        	
        	JSONStringer stringer = new JSONStringer();
        	try {
        		if(isTest){
        			stringer.object()
	    			.key("jvm_cpu_support").value(true)
	    			.key("system_cpu_support").value(true)
	    			.key("load_avg_support").value(true)
	    			.key("jvm_memory_support").value(true)
	    			.endObject();
	//    			logger.debug("stringer = "+stringer);
	    			w.println(stringer.toString());
        		}else{
        			stringer.object()
	    			.key("jvm_cpu_support").value(service.isJvmCpuInfoSupported())
	    			.key("system_cpu_support").value(service.isSystemCpuInfoSupported())
	    			.key("load_avg_support").value(service.isLoadAvgInfoSupported())
	    			.key("jvm_memory_support").value(service.isJvmMemoryInfoSupported())
	    			.endObject();
	//    			logger.debug("stringer = "+stringer);
	    			w.println(stringer.toString());
        		}
    		} catch (JSONException e) {
    			throw new ServletException("JSONException 발생",e);
    		}
    	}else{
    		String callback = request.getParameter("jsoncallback");
        	response.setStatus(HttpServletResponse.SC_OK);
        	
    		if(resultType == JSONP_TYPE) {
    			response.setContentType("text/javascript;");
    			
    		}else{
    			response.setContentType("application/json;");
    		}
    		JvmCpuInfo cpuInfo = service.getJvmCpuInfo();
    		JvmMemoryInfo memoryInfo = service.getJvmMemoryInfo();
    		//결과생성
    		JSONStringer stringer = new JSONStringer();
    		try {
    			if(simple != null){
    				if(isTest){
	    				stringer.object()
	    				.key("cj").value(r.nextInt(60))
		    			.key("la").value(r.nextFloat()*2.0f)
		    			.key("mx").value(2048)
		    			.key("mu").value(1024 + r.nextInt(100))
		    			.endObject();
    				}else{
    					stringer.object()
	    				.key("cj").value(cpuInfo.jvmCpuUse)
		    			.key("la").value(cpuInfo.systemLoadAverage)
		    			.key("mx").value(memoryInfo.maxHeapMemory + memoryInfo.maxNonHeapMemory)
		    			.key("mu").value(memoryInfo.usedHeapMemory + memoryInfo.usedNonHeapMemory)
		    			.endObject();
    				}
    			}else{
	    			if(isTest){
	    				stringer.object()
		    			.key("cj").value(r.nextInt(60))
		    			.key("cs").value(r.nextInt(30))
		    			.key("la").value(r.nextFloat()*2.0f)
		    			.key("mhm").value(2048)
		    			.key("mhc").value(1024)
		    			.key("mhu").value(512 + r.nextInt(500))
		    			.key("mnm").value(128)
		    			.key("mnc").value(64)
		    			.key("mnu").value(32 + r.nextInt(32))
		    			.endObject();
	    			}else{
		    			stringer.object()
		    			.key("cj").value(cpuInfo.jvmCpuUse)
		    			.key("cs").value(cpuInfo.systemCpuUse)
		    			.key("la").value(cpuInfo.systemLoadAverage)
		    			.key("mhm").value(memoryInfo.maxHeapMemory)
		    			.key("mhc").value(memoryInfo.committedHeapMemory)
		    			.key("mhu").value(memoryInfo.usedHeapMemory)
		    			.key("mnm").value(memoryInfo.maxNonHeapMemory)
		    			.key("mnc").value(memoryInfo.committedNonHeapMemory)
		    			.key("mnu").value(memoryInfo.usedNonHeapMemory)
		    			.endObject();
	    			}
    			}
//    			logger.debug("stringer = "+stringer);
    		} catch (JSONException e) {
    			throw new ServletException("JSONException 발생",e);
    		}
    		
    		//JSONP는 데이터 앞뒤로 function명으로 감싸준다.
    		if(resultType == JSONP_TYPE) {
        		w.write(callback+"(");
        	}
    		
    		//정보를 보내준다.
    		w.write(stringer.toString());
    		
    		if(resultType == JSONP_TYPE) {
        		w.write(");");
        	}
    		
    	}
    	w.close();
    	
    }
  
}
