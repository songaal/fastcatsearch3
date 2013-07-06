<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 
<%
	String FASTCAT_MANAGE_ROOT = request.getContextPath() + "/";
	String FASTCAT_SEARCH_ROOT = request.getContextPath() + "/";
	{
		if(application.getAttribute("FASTCAT_MANAGE_ROOT")!=null) {
			FASTCAT_MANAGE_ROOT = (String)application.getAttribute("FASTCAT_MANAGE_ROOT")+"/";
		}
		if(application.getAttribute("FASTCAT_SEARCH_ROOT")!=null) {
			FASTCAT_SEARCH_ROOT = (String)application.getAttribute("FASTCAT_SEARCH_ROOT")+"/";
		}
	}
%>
