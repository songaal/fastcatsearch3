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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.servlet.WebServiceHttpServlet;
import org.fastcatsearch.statistics.RealTimeCollectionStatistics;
import org.fastcatsearch.statistics.StatisticsInfoService;
import org.json.JSONException;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 데이터 갱신은 StatisticsInfoService 에서 주기적으로 수행하며, 이 servlet은 데이터만 가져가게됨.
 * @author swsong
 *
 */
public class StatisticsInfoServlet extends WebServiceHttpServlet {
	
	private static final long serialVersionUID = 963640595944747847L;
	
	public StatisticsInfoServlet() {
	}
	
    public StatisticsInfoServlet(int resultType){
    	super(resultType);
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	doGet(request, response);
    }
    
    Random r = new Random(System.currentTimeMillis());
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	StatisticsInfoService statisticsInfoService = ServiceManager.getInstance().getService(StatisticsInfoService.class);
    	
    	if(!statisticsInfoService.isRunning()){
    		throw new ServletException(statisticsInfoService.getClass().getName()+"이 시작중이 아닙니다.");
    	}
    	
    	String q = request.getParameter("q");
    	String simple = request.getParameter("simple");
    	String test = request.getParameter("test");
    	boolean isTest = false;
    	if(test != null && test.equals("true")){
    		isTest = true;
    	}
    	PrintWriter w = response.getWriter();
    	if(q != null){
    		//지원사항을 알려준다.
    		response.setCharacterEncoding("utf-8");
        	response.setStatus(HttpServletResponse.SC_OK);
        	response.setContentType("application/json;");
        	
        	String[] collectionNameList = statisticsInfoService.getCollectionNameList();
        	boolean[] status = statisticsInfoService.getCollectionStatus();
        	
        	JSONStringer stringer = new JSONStringer();
        	try {
        		stringer.array();
        		if(collectionNameList != null){
        			for (int i = 0; i < collectionNameList.length; i++) {
        				stringer.object()
        				.key("name").value(collectionNameList[i]);
        				if(isTest){
        					stringer.key("status").value(true);
        				}else{
        					stringer.key("status").value(status[i]);
        				}
        				stringer.endObject();
        			}
        		}
        		stringer.endArray();
//    			logger.debug("stringer = "+stringer);
    			w.println(stringer.toString());
    		} catch (JSONException e) {
    			throw new ServletException("JSONException 발생",e);
    		}
    	}else{
    		String callback = request.getParameter("jsoncallback");
    		
    		response.setCharacterEncoding("utf-8");
        	response.setStatus(HttpServletResponse.SC_OK);
        	
    		if(resultType == JSONP_TYPE) {
    			response.setContentType("text/javascript;");
    		}else{
    			response.setContentType("application/json;");
    		}
    		String[] collectionNameList = statisticsInfoService.getCollectionNameList();
        	boolean[] status = statisticsInfoService.getCollectionStatus();
    		RealTimeCollectionStatistics[] statistics = statisticsInfoService.getCollectionStatisticsList();
    		RealTimeCollectionStatistics globalCollectionStatistics = statisticsInfoService.getGlobalCollectionStatistics();
    		//결과생성
    		JSONStringer stringer = new JSONStringer();
    		try {
    			//
    			//간략정보
    			//
    			if(simple != null){
    				if(isTest){
    					stringer.object();
    					int hit = r.nextInt(30)+1;
						stringer.key("h").value(r.nextInt(30)+1);
						stringer.key("fh").value(r.nextInt(hit > 5 ? 5 : hit));
						int ta = r.nextInt(50) + 1;
						stringer.key("ta").value(ta + 1);
						stringer.key("tx").value(ta+r.nextInt(20) + 1);
						stringer.endObject();
					}else{
						//컬렉션 통합 정보를 리턴한다.
//	    				if(collectionNameList != null){
//	    					int hit = 0;
//	    					int failHit = 0;
//	    					int avgTime = 0;
//	    					int maxTime = 0;
//	    					int count = 0;
//	    					for (int i = 0; i < collectionNameList.length; i++) {
//	    						if(status[i]){
//	    							count++;
//	    							hit += statistics[i].getHitPerUnitTime();
//	    							failHit += statistics[i].getFailHitPerUnitTime();
//	    							avgTime += statistics[i].getMeanResponseTime();
//	    							if(statistics[i].getMaxResponseTime() > maxTime){
//	    								maxTime = statistics[i].getMaxResponseTime();
//	    							}
//	    						}
//	    					}
//	    				}
	    				stringer.object();
						stringer.key("h").value(globalCollectionStatistics.getHitPerUnitTime());
						stringer.key("fh").value(globalCollectionStatistics.getFailHitPerUnitTime());
						stringer.key("ach").value(globalCollectionStatistics.getAccumulatedHit());
						stringer.key("acfh").value(globalCollectionStatistics.getAccumulatedFailHit());
						stringer.key("ta").value(globalCollectionStatistics.getMeanResponseTime());
						stringer.key("tx").value(globalCollectionStatistics.getMaxResponseTime());
						stringer.endObject();
					}
    			}else{
				//
				//컬렉션별 상세정보
				//
    				stringer.array();
    				if(collectionNameList != null){
    					for (int i = 0; i < collectionNameList.length; i++) {
    						if(status[i]){
    							stringer.object()
    							.key("c").value(collectionNameList[i]);
    							if(isTest){
    								int hit = r.nextInt(10)+1;
    								stringer.key("h").value(hit);
    								stringer.key("fh").value(r.nextInt(hit > 3 ? 3 : hit));
    								stringer.key("ta").value(r.nextInt(100) + 1);
    								stringer.key("tx").value(r.nextInt(50)+r.nextInt(20) + 1);
    							}else{
    								stringer.key("h").value(statistics[i].getHitPerUnitTime());
    								stringer.key("fh").value(statistics[i].getFailHitPerUnitTime());
    								stringer.key("ach").value(statistics[i].getAccumulatedHit());
    								stringer.key("acfh").value(statistics[i].getAccumulatedFailHit());
    								stringer.key("ta").value(statistics[i].getMeanResponseTime());
    								stringer.key("tx").value(statistics[i].getMaxResponseTime());
    							}
    							stringer.endObject();
    						}
    					}
    				}
    				stringer.endArray();
    			}
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
