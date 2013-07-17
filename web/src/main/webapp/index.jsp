<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="org.fastcatsearch.web.*"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.net.URLDecoder"%>
<%@include file="webroot.jsp"%>
<%
	String message = URLDecoder.decode(WebUtils.getString(request.getParameter("message"), ""),"utf-8");
	String cmd = request.getParameter("cmd");
	if ("ajax".equals(cmd)) {
		String location = request.getParameter("location");
		try {
			URL url = new URL(location);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				out.println(line);
			}
		} catch (Exception e) { }

	}else{
		response.sendRedirect(FASTCAT_MANAGE_ROOT+"main.jsp");
	}

%>
