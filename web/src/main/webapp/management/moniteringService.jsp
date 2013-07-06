<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="java.util.Properties"%>
<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.fastcatsearch.job.IncIndexJob"%>
<%@page import="org.fastcatsearch.job.FullIndexJob"%>
<%@page import="org.fastcatsearch.control.JobService"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.db.dao.SearchEvent"%>
<%@page import="org.fastcatsearch.log.EventDBLogger"%>
<%@page import="org.fastcatsearch.management.ManagementInfoService"%>
<%@page import="org.fastcatsearch.statistics.StatisticsInfoService"%>
<%@page import="org.fastcatsearch.server.CatServer"%>
<%@page import="java.util.*"%>
<%@page import="org.json.*"%>
<%@include file="../common.jsp" %>

<%
	int cmd = Integer.parseInt(request.getParameter("cmd"));

switch(cmd){
	case 0:
	{
		String idStr = request.getParameter("id") == null ? "" : request.getParameter("id");
		if(!"".equals(idStr)){
	int id = Integer.parseInt(idStr);
	String status = request.getParameter("status") == null ? "" : request.getParameter("status");
	int result = DBService.getInstance().SearchEvent.update(id, status);
	//DBHandler.getInstance().commit();
	if(result < 0){
		out.print("fail");
	}else{
		out.print("success");
	}
		}
		
		break;
	}
	
}
%>
