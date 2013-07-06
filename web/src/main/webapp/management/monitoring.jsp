<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="org.fastcatsearch.db.dao.SearchEvent"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.db.dao.JobHistory"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.log.EventDBLogger"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLDecoder"%>
<%@include file="../common.jsp" %>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%
	String cmd = request.getParameter("cmd");
	String type = request.getParameter("type");
	String collection = WebUtils.getString(request.getParameter("collection"),"");
	if(type == null){
		type = "minute";
	}
	String message = "";
	if ("login".equals(cmd)) {
		String username = request.getParameter("username");
		String passwd = request.getParameter("passwd");
		String[] accessLog = IRSettings.isCorrectPasswd(username, passwd);
		if(accessLog != null){
			//로긴 성공
			session.setAttribute("authorized", username);
			session.setAttribute("lastAccessLog", accessLog);
			session.setMaxInactiveInterval(60 * 30); //30 minutes
			IRSettings.storeAccessLog(username, ""); //ip주소는 공란으로 남겨두고 사용하지 않도록함. 
			//request.getRemoteAddr()로는 제대로된 사용자 ip를 알아낼수 없음.
			//jetty에서는 getHeader("REMOTE_ADDR"); 또는 req.getHeaer("WL-Proxy-Client-IP")+","+req.getHeaer("Proxy-Client-IP")+","+req.getHeaer("X-Forwarded-For")) 등을 제공하지 않는다.
			message = "";
		}else{
			message = "아이디와 비밀번호를 확인해주세요.";
		}
		
	}else if ("logout".equals(cmd)) {
		session.invalidate();
		response.sendRedirect(FASTCAT_MANAGE_ROOT+"index.jsp");
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>FASTCAT 검색엔진 관리도구</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/reset.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style.css" rel="stylesheet" type="text/css" />
<script src="<%=FASTCAT_MANAGE_ROOT%>js/amcharts/amcharts.js" type="text/javascript"></script>
<script src="<%=FASTCAT_MANAGE_ROOT%>js/amcharts/raphael.js" type="text/javascript"></script>
<!--[if lte IE 6]>
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style_ie.css" rel="stylesheet" type="text/css" />
<![endif]-->
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/common.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/detailMoni.js"></script>
<script>
	$(document).ready(function() {
		var message = "<%=message %>";
		if(message != "")
			alert(message);
	});

	function logout(){
		location.href="?cmd=logout";
	}
</script>
</head>
<body>
<div id="container">
<!-- header -->
<%@include file="../header.jsp" %>
		
<div id="mainContent">
		<div style=width:100%;height:30px;">
			<a class="btn_s"  href="<%=FASTCAT_MANAGE_ROOT%>monitering.jsp?type=minute">분별</a>
			<a class="btn_s"  href="<%=FASTCAT_MANAGE_ROOT%>monitering.jsp?type=hour">시간별</a>
		</div>
		<div style=width:100%;height:20px;">
			
			<%
				if("minute".equals(type)){
					%>
			<select name="time" id="time" onchange="javascript:refreshChart('minute')">		
					<%
					for(int i=0; i<24; i++){
						%>
						<option value="<%=i%>" ><%=i%>시</option>
						<%
					}
				}else if("hour".equals(type)){
					%>
			<select name="time" id="time" onchange="javascript:refreshChart('hour')">		
					<%
					for(int i=1; i<31; i++){
						%>
						<option value="<%=i%>" ><%=i%>일</option>
						<%
					}
				}
			%>
			</select>
		</div>
		<div style=width:100%;height:200px;">
			<div id="chartJCPUDiv" style=" height:200px; background-color:#FFFFFF"></div>
		</div>
		<div style=width:100%;height:200px;">
			<div id="chartMemDiv" style=" height:200px; background-color:#FFFFFF"></div>
		</div>
		<div style=width:100%;height:200px;">
			<div id="chartLoadDiv" style=" height:200px; background-color:#FFFFFF"></div>
		</div>
		<div style=width:100%;height:20px;">
			<%
					IRConfig irConfig = IRSettings.getConfig();
					String collectinListStr = irConfig.getString("collection.list");
					String[] colletionList = collectinListStr.split(",");
					String refreshStr = "";
					if("minute".equals(type)){
						refreshStr = "minute";
					}else{
						refreshStr = "hour";
					}
			%>
			<select name="collection" id="collection" onchange="javascript:refreshChart('<%=refreshStr%>')">
			<% for(int k=0; k<colletionList.length; k++){ %>
			<option value="<%=colletionList[k] %>" <%=colletionList[k].equals(collection) ? "selected" : "" %> ><%=colletionList[k] %></option>
			<% } %>
			</select>
		</div>
		<div style=width:100%;height:200px;">
			<div id="chartSearchActDiv" style=" height:200px; background-color:#FFFFFF"></div>
		</div>
		<div style=width:100%;height:200px;">
			<div id="chartSearchTimeDiv" style=" height:200px; background-color:#FFFFFF"></div>
		</div>
		<div style=width:100%;height:200px;">
			<div id="chartSearchFailDiv" style=" height:200px; background-color:#FFFFFF"></div>
		</div>
<!-- E : #mainContent -->
</div>

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
</body>
</html>
