<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>
<%@ page contentType="text/html; charset=UTF-8"%> 
<%@page import="org.fastcatsearch.job.DictionaryCompileApplyJob"%>
<%@page import="org.fastcatsearch.job.HashSetDictionaryCompileApplyJob"%>
<%@page import="org.fastcatsearch.job.HashMapDictionaryCompileApplyJob"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="org.fastcatsearch.job.DictionaryReloadJob"%>
<%@page import="org.fastcatsearch.ir.dic.HashMapDictionary"%>
<%@page import="org.fastcatsearch.ir.dic.HashSetDictionary"%>
<%@page import="org.fastcatsearch.control.*"%>

<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.config.Schema"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.datasource.DataSourceSetting"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.db.DBService" %>
<%@page import="org.fastcatsearch.service.*" %>
<%@page import="org.fastcatsearch.db.vo.*" %>
<%@page import="org.fastcatsearch.db.dao.*" %>
<%@page import="java.io.File"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.*"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.net.URLDecoder"%>
<%@include file="../common.jsp" %>

<%
	int cmd = Integer.parseInt(request.getParameter("cmd"));
	String category = request.getParameter("category");
	String dic = request.getParameter("dic");
	
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	String synonymDictionaryId = category + "SynonymDictionary";
	String userDictionaryId = category + "UserDictionary";
	String stopDictionaryId = category + "StopDictionary";
	SetDictionary synonymDictionary = dbHandler.getDAO(synonymDictionaryId);
	SetDictionary userDictionary = dbHandler.getDAO(userDictionaryId);
	SetDictionary stopDictionary = dbHandler.getDAO(stopDictionaryId);
	
switch(cmd){
	case 0:
	{
		
	break;
	}
	case 1:
	{
		//ADD SYNONYM
		String newword = request.getParameter("newword");
		
		int count = synonymDictionary.insert(newword);
		if(count == 0){
		}else{
		}
		request.getRequestDispatcher("synonymDic.jsp").forward(request, response);
		break;
	}
	case 2:
	{
		//UPDATE SYNONYM
		int id = WebUtils.getInt(request.getParameter("id"), -1);
		String newword = request.getParameter("newword");
		int count = synonymDictionary.update(id, newword);
		request.getRequestDispatcher("synonymDic.jsp").forward(request, response);
		break;
	}
	case 3:
	{
		//ADD custom word
		String customword = request.getParameter("customwordReal");
		
		userDictionary.insert(customword);
		//dbHandler.commit();
		//response.sendRedirect("userDic.jsp");
		request.getRequestDispatcher("userDic.jsp").forward(request, response);
		break;
	}
	
	case 4:
	{
		//DELETE custom word
		String[] selectedValues = request.getParameterValues("checkGroup");
		if(selectedValues != null){
			for(int i=0;i<selectedValues.length;i++){
				userDictionary.delete(selectedValues[i]);
			}
			//dbHandler.commit();
			//response.sendRedirect("userDic.jsp");
		}else{
			//response.sendRedirect("userDic.jsp");
		}
		request.getRequestDispatcher("userDic.jsp").forward(request, response);
		break;
	}
	case 21:
	{
		//유사어삭제.
		String[] selectedValues = request.getParameterValues("checkGroup");
		if(selectedValues != null){
			for(int i=0;i<selectedValues.length;i++){
				try{
					synonymDictionary.deleteById(Integer.parseInt(selectedValues[i]));
				}catch(Exception ignore){
				}
			}
		}
		request.getRequestDispatcher("synonymDic.jsp").forward(request, response);
		break;
	}
	case 22:
	{  ///유사어 추가.
		String synonymWord = request.getParameter("synonymWord");
		
		synonymDictionary.insert(synonymWord);
		request.getRequestDispatcher("synonymDic.jsp").forward(request, response);
	break;
	}
	case 5:
	{
		String keyword = URLDecoder.decode(WebUtils.getString(request.getParameter("keyword"),""),"utf-8");
		
		List<SetDictionaryVO>	result = synonymDictionary.selectWithExactKeyword(keyword);
		if(result.size() != 0){
			SetDictionaryVO sd = result.get(0);
			out.clear();
			out.print(sd.keyword);
		}else{
			out.clear();
			out.print("");
		}
		break;
	}
	case 6:
	{
		String bannedword = request.getParameter("bannedwordReal");
		
		stopDictionary.insert(bannedword);
		//dbHandler.commit();
		//response.sendRedirect("stopDic.jsp");
		request.getRequestDispatcher("stopDic.jsp").forward(request, response);
	break;
	}
	
	case 7:
	{
		String[] selectedValues = request.getParameterValues("checkGroup");
		if(selectedValues != null){
			for(int i=0;i<selectedValues.length;i++){
				stopDictionary.delete(selectedValues[i]);
			}
			//dbHandler.commit();
			//response.sendRedirect("stopDic.jsp");
		}else{
			//response.sendRedirect("stopDic.jsp");
		}
		request.getRequestDispatcher("stopDic.jsp").forward(request, response);
	break;
	}
	/* case 8:
	{
	String keyword = URLDecoder.decode(WebUtils.getString(request.getParameter("keyword"),""),"utf-8");
		
		DBHandler dbHandler = DBHandler.getInstance();
		RecommendKeyword rw = dbHandler.RecommendKeyword.selectWithKeyword(keyword);
		if(rw != null){
	out.clear();
	out.print(rw.value);
		}else{
	out.clear();
	out.print("");
		}	
	break;
	} */
	case 9:
	{
		String basicword = request.getParameter("basicwordReal");
		
		//dbHandler.BasicDictionary.insert(basicword);
		//dbHandler.commit();
		//response.sendRedirect("systemDic.jsp");
		request.getRequestDispatcher("systemDic.jsp").forward(request, response);
	break;
	}
	
	case 10:
	{
		String[] selectedValues = request.getParameterValues("checkGroup");
		if(selectedValues != null){
			for(int i=0;i<selectedValues.length;i++){
				//dbHandler.BasicDictionary.delete(selectedValues[i]);
			}
			//dbHandler.commit();
			//response.sendRedirect("systemDic.jsp");
		}else{
			//response.sendRedirect("systemDic.jsp");
		}
		request.getRequestDispatcher("systemDic.jsp").forward(request, response);
		break;
	}
	case 11:
	{
		/* DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
		try{
			if(dbHandler.BasicDictionary.bulkInsert(new File(IRSettings.HOME+"dic/src/korean.txt"))){
				//성공
				out.print(0);
			}else{
				//실패
				out.print(1);
				}
		}catch(Exception e){
			e.printStackTrace();
			out.print(1);
		} */
		out.print(1);
		break;
	}
	case 12:
	{
		String deleteword = request.getParameter("deleteword");
		synonymDictionary.delete(deleteword);
		//dbHandler.commit();
		//response.sendRedirect("synonymDic.jsp");
		request.getRequestDispatcher("systemDic.jsp").forward(request, response);
		break;
	}
	case 13:
	{
		String theword = request.getParameter("theword");
		
		List<SetDictionaryVO> list = synonymDictionary.selectWithExactKeyword(theword);
		
		if(list.size() > 0){
			String value = list.get(0).keyword;
		    response.getWriter().write(value);
		}else{
			response.getWriter().write("");
		}
		break;
	}
	case 14:
	{
		//사전 컴파일 및 교체
		DictionaryCompileApplyJob job = new DictionaryCompileApplyJob();
		job.setArgs(new String[]{category, dic});
		ResultFuture result = JobService.getInstance().offer(job);
		Object obj = result.take();
		if(result.isSuccess()){
			out.print(0);
		}else{
			out.print(1);
		}
		break;
	}
	case 15:
	{
		//사전 교체
		DictionaryReloadJob job = new DictionaryReloadJob();
		job.setArgs(new String[]{dic});
		JobService.getInstance().offer(job);
		break;
	}
	case 20:
	{
		//사전 초기화(모두삭제)
		if(dic.equals("synonymDic")){
			synonymDictionary.truncate();
			request.getRequestDispatcher("synonymDic.jsp").forward(request, response);
		}else if(dic.equals("userDic")){
			userDictionary.truncate();
			request.getRequestDispatcher("userDic.jsp").forward(request, response);
		}else if(dic.equals("stopDic")){
			stopDictionary.truncate();
			request.getRequestDispatcher("stopDic.jsp").forward(request, response);
		}
		
		break;
	}
	
	case 99:
		//test용도.
	{
		synonymDictionary.dropTable();
		
		break;
	}
	
	
	default:
	break;
}
%>
