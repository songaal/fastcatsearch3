<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="org.fastcatsearch.db.dao.SetDictionary"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.io.File"%>

<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.service.*"%>
<%-- <%@page import="org.fastcatsearch.keyword.KeywordHit"%>
<%@page import="org.fastcatsearch.keyword.KeywordFail"%> --%>
<%@include file="../common.jsp"%>

<%
	int cmd = Integer.parseInt(request.getParameter("cmd"));

switch(cmd){
	
	case 1:
	{
		//ADD recommend
		DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
		SetDictionary recommendKeyword = dbHandler.getDAO("RecommendKeyword", SetDictionary.class);
		String newword = request.getParameter("recommendWord");
		int count = recommendKeyword.insert(newword);
		request.getRequestDispatcher("recommend.jsp").forward(request, response);
		break;
	}
	case 2:
	{
		//UPDATE recommend
		DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
		SetDictionary recommendKeyword = dbHandler.getDAO("RecommendKeyword", SetDictionary.class);
		int id = WebUtils.getInt(request.getParameter("id"), -1);
		String newword = request.getParameter("recommendWord");
		
		int count = recommendKeyword.update(id, newword);
		request.getRequestDispatcher("recommend.jsp").forward(request, response);
		break;
	}
	case 3:
	{
		//recommend 모두 지우기.
		String target = request.getParameter("target");
		if(target.equals("recommend")){
			DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
			SetDictionary recommendKeyword = dbHandler.getDAO("RecommendKeyword", SetDictionary.class);
			recommendKeyword.truncate();
			request.getRequestDispatcher("recommend.jsp").forward(request, response);
		}
		break;
	}
	case 12:
	{
		String deleteword = request.getParameter("deleteword");
		DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
		SetDictionary recommendKeyword = dbHandler.getDAO("RecommendKeyword", SetDictionary.class);
		/* recommendKeyword.delete(deleteword);
		response.sendRedirect("recommend.jsp");
		break; */
		String[] selectedValues = request.getParameterValues("checkGroup");
		if(selectedValues != null){
			for(int i=0;i<selectedValues.length;i++){
				try{
					recommendKeyword.deleteById(Integer.parseInt(selectedValues[i]));
				}catch(Exception ignore){
				}
			}
		}
		request.getRequestDispatcher("recommend.jsp").forward(request, response);
	}
	/* case 13:
	{
		String keyword = URLDecoder.decode(WebUtils.getString(request.getParameter("keyword"),""),"utf-8");
		if(!"".equals(keyword)) {
	String used = request.getParameter("used");
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	dbHandler.KeywordHit.setNotUsing(keyword, ("y".equals(used)?0:1));
	//dbHandler.commit();
		}
		break;
	}
	case 14:
	{
		int uprows = 0;
		String keyword = URLDecoder.decode(WebUtils.getString(request.getParameter("keyword"),""),"utf-8");
		if(!"".equals(keyword)) {
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	uprows = dbHandler.KeywordHit.deleteKeyword(keyword);
	//dbHandler.commit();
		}
		if(uprows==0) {
	out.println("keyword not found ["+keyword+"]");
		}
		break;
	}
	case 15:
	{
		int uprows = 0;
		String keyword1 = URLDecoder.decode(WebUtils.getString(request.getParameter("keyword1"),""),"utf-8");
		String keyword2 = URLDecoder.decode(WebUtils.getString(request.getParameter("keyword2"),""),"utf-8");
		if(!"".equals(keyword1) && !"".equals(keyword2)) {
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	uprows = dbHandler.KeywordHit.modifyKeyword(keyword1, keyword2);
	//dbHandler.commit();
		}
		if(uprows==-1) {
	out.println("-1");
		}
		break;
	}
	case 16:
	{
		int uprows = 0;
		String keyword = URLDecoder.decode(WebUtils.getString(request.getParameter("keyword"),""),"utf-8");
		int type = Integer.parseInt(request.getParameter("type"));
		int popular = Integer.parseInt(request.getParameter("popular"));
		if(!"".equals(keyword)) {
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	uprows = dbHandler.KeywordHit.updateKeywordPopular(keyword, type, popular);
	//dbHandler.commit();
		}
		if(uprows==-1) {
	out.println("-1");
		}
		break;
	}
	case 17:
	{
		int uprows = 0;
		String newkeyword = URLDecoder.decode(WebUtils.getString(request.getParameter("newkeyword"),""),"utf-8");
		int newkeywordpop = Integer.parseInt(request.getParameter("newkeywordpop"));
		int newkeywordhit = Integer.parseInt(request.getParameter("newkeywordhit"));
		if(!"".equals(newkeyword)) {
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	if(dbHandler.KeywordHit.countKeywordItem(newkeyword) == 0) {
		uprows = dbHandler.KeywordHit.insert(KeywordHit.POPULAR_ACCUM, 0, newkeyword, newkeywordhit, newkeywordpop, 999, true, new java.util.Date(), new java.util.Date());
		//dbHandler.commit();
	} else {
		out.print("1");
	}
		}
		if(uprows==-1) {
	out.println("-1");
		}
		break;
	}
	case 18:
	{
		int uprows = 0;
		String newkeyword = URLDecoder.decode(WebUtils.getString(request.getParameter("newkeyword"),""),"utf-8");
		int newkeywordpop = Integer.parseInt(request.getParameter("newkeywordpop"));
		int newkeywordhit = Integer.parseInt(request.getParameter("newkeywordhit"));
		if(!"".equals(newkeyword)) {
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	if(dbHandler.KeywordFail.countKeywordFailItem(newkeyword) == 0) {
		uprows = dbHandler.KeywordFail.insert(KeywordFail.POPULAR_ACCUM, 0, newkeyword, newkeywordhit, newkeywordpop, 999, true, new java.util.Date(), new java.util.Date());
		//dbHandler.commit();
	} else {
		out.print("1");
	}
		}
		if(uprows==-1) {
	out.println("-1");
		}
		break;
	} */
	
	default:
		break;
}
%>
