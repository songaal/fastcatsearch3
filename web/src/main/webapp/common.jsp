<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="com.fastcatsearch.util.*"%>
<%@include file="webroot.jsp"%>
<%
	request.setCharacterEncoding("UTF-8"); 
%>

<%
	if (IRSettings.isAuthUsed() && session.getAttribute("authorized") == null) {
		response.sendRedirect(FASTCAT_MANAGE_ROOT+"index.jsp?message="+URLEncoder.encode(URLEncoder.encode("로그인이 필요합니다.", "utf-8"),"utf-8"));
		return;
	}
%>
