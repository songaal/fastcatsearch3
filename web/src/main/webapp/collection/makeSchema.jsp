<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="java.util.*"%>
<%@page import="java.text.*"%>
<%@page import="java.net.*"%>
<%@page import="java.io.*"%>
<%@page import="java.sql.*"%>
<%@page import="java.sql.Types.*"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.ir.common.IRException"%>
<%@page import="org.fastcatsearch.ir.config.DataSourceConfig"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.document.DocumentReader"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.util.MakeSchema"%>	 
<%boolean result = false;
try
{
	String collection=request.getParameter("collection");
	result = MakeSchema.makeSchema(collection);
	out.print("success");
}
catch ( Exception e )
{
	out.print("failed!");
}
%>
