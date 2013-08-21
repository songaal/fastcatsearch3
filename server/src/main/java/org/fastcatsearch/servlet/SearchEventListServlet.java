///*
// * Copyright (c) 2013 Websquared, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the GNU Public License v2.0
// * which accompanies this distribution, and is available at
// * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
// * 
// * Contributors:
// *     swsong - initial API and implementation
// */
//
//package org.fastcatsearch.servlet;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.List;
//import java.util.Random;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.fastcatsearch.db.DBService;
//import org.fastcatsearch.db.dao.SearchEvent;
//import org.fastcatsearch.db.vo.SearchEventVO;
//import org.fastcatsearch.log.EventDBLogger;
//import org.fastcatsearch.service.ServiceManager;
//import org.fastcatsearch.servlet.WebServiceHttpServlet;
//import org.fastcatsearch.statistics.StatisticsInfoService;
//import org.json.JSONException;
//import org.json.JSONStringer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//
///**
// * 데이터 갱신은 ManagementInfoService 에서 주기적으로 수행하며, 이 servlet은 데이터만 가져가게됨.
// * @author swsong
// *
// */
//public class SearchEventListServlet extends WebServiceHttpServlet {
//	
//	private static final long serialVersionUID = -2389828881575351283L;
//	
//	public SearchEventListServlet() {
//	}
//	
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//    	doGet(request, response);
//    }
//    Random r = new Random(System.currentTimeMillis());
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//    	String q = request.getParameter("q");
//    	
//		//지원사항을 알려준다.
//		response.setCharacterEncoding("utf-8");
//    	response.setStatus(HttpServletResponse.SC_OK);
//    	response.setContentType("application/json;");
//    	
//    	StatisticsInfoService statisticsInfoService = ServiceManager.getInstance().getService(StatisticsInfoService.class);
//    	
//    	JSONStringer stringer = new JSONStringer();
//    	PrintWriter w = response.getWriter();
//    	
//    	if(q != null){
//    		long time = 0;
//    		try{
//    			time = Long.parseLong(q);
//    		}catch(Exception e){
//    			time = System.currentTimeMillis();
//    		}
//    		//업데이트 여부 정보만 전송한다.
//    		response.setContentType("application/json;");
//			try {
//    			stringer.object()
//    			.key("update").value(statisticsInfoService.isEventUpdated(time))
//    			.endObject();
//			} catch (JSONException e) {
//				throw new ServletException("JSONException 발생",e);
//			}
//    	}else{
//			response.setContentType("application/json;");
//	    	
//			String lenStr = request.getParameter("length") == null ? "5" : request.getParameter("length");
//			int len = Integer.parseInt(lenStr);
//			List<SearchEventVO> searchEventList = DBService.getInstance().getDAO("SearchEvent", SearchEvent.class).select(1, len);
//			
//			try {
//				stringer.array();
//				for(int i=0;i<searchEventList.size();i++){
//					stringer.object();
//					SearchEventVO searchEvent = searchEventList.get(i);	
//					stringer.key("id").value(searchEvent.id);
//					stringer.key("when").value(searchEvent.when.toString().substring(0, 19));
//					stringer.key("type").value(searchEvent.type);
//					stringer.key("category").value(EventDBLogger.getCateName(searchEvent.category));
//					stringer.key("summary").value(searchEvent.summary);
//					stringer.key("status").value(searchEvent.status);
//					stringer.key("stacktrace").value(searchEvent.stacktrace);
//					stringer.endObject();
//				}
//				stringer.endArray();
//	    	
//			} catch (JSONException e) {
//				throw new ServletException("JSONException 발생",e);
//			}
//    	}//if(q != null)
//    		
//		
//		//정보를 보내준다.
//		w.write(stringer.toString());
//		
//    	w.close();
//    	
//    }
//  
//}
