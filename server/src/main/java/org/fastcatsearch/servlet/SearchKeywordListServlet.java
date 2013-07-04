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

import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.servlet.WebServiceHttpServlet;
import org.fastcatsearch.statistics.SearchKeywordCache;
import org.fastcatsearch.statistics.StatisticsInfoService;
import org.json.JSONException;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 데이터 갱신은 ManagementInfoService 에서 주기적으로 수행하며, 이 servlet은 데이터만 가져가게됨.
 * @author swsong
 *
 */
public class SearchKeywordListServlet extends WebServiceHttpServlet {
	
	private static final long serialVersionUID = -2389828881575351283L;
	private static String[] TEST_KEYWORDS = "잠비아는 12일(현지시간) 아프리카 가봉의 스타드 당곤제 경기장에서 열린 코트디부아르와의 2012 아프리카 컵 오브 네이션스(아프리카네이션스 컵) 결승전에서 승부차기로 8-7 승리를 거뒀다. 1974년부터 이 대회에 참가한 잠비아는 사상 처음으로 우승컵을 거머쥐는 영예를 안았다. 국제축구연맹(FIFA) 랭킹으로 잠비아는 71위, 코트디부아르는 18위로 객관적인 전력에선 잠비아가 크게 밀렸지만 결과는 랭킹과는 다르게 나왔다. 잠비아는 특히 1993년 4월27일 축구 국가대표를 태운 비행기가 추락했던 아프리카 가봉의 리브르빌에서 우승컵을 들어 올려 우승의 의미가 더 컸다. 코트디부아르는 대회 결승에 3차례 올랐지만 정규 경기 시간에 단 1골도 넣지 못하는 징크스를 이어 갔다. 3번의 결승전에서 모두 승부차기까지 갔던 코트디부아르는 1992년 가나를 상대로 한 결승에선 12명의 키커를 내보낸 끝에 11-10으로 간신히 우승했다. 2006년에는 승부차기에서 2-4로 이집트에 패한 적이 있다.".split(" ");
	
	public SearchKeywordListServlet() {
	}
	
    public SearchKeywordListServlet(int resultType){
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
    	
    	String callback = request.getParameter("jsoncallback");
    	String test = request.getParameter("test");
    	boolean isTest = false;
    	if(test != null && test.equals("true")){
    		isTest = true;
    	}
    	
		//지원사항을 알려준다.
		response.setCharacterEncoding("utf-8");
    	response.setStatus(HttpServletResponse.SC_OK);
    	response.setContentType("application/json;");
    	
    	PrintWriter w = response.getWriter();
    	SearchKeywordCache keywordCache = statisticsInfoService.getKeywordCache();
    	JSONStringer stringer = new JSONStringer();
    	String[] list = keywordCache.getKeywordList();
    	int count = keywordCache.getCount();
    	try {
    		stringer.array();
    		if(isTest){
    			count = r.nextInt(9) + 1;
    			
    			for (int i = 0; i < count; i++) {
    				int idx = r.nextInt(TEST_KEYWORDS.length);
    				stringer.object().key("key").value(TEST_KEYWORDS[idx]).endObject();
    			}
    		}else{
    			for (int i = 0; i < count; i++) {
    				stringer.object().key("key").value(list[i]).endObject();
    			}
    		}
    		stringer.endArray();
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
		
    	w.close();
    	
    }
  
}
