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
<%
	String cmd = request.getParameter("cmd");
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
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/monitoring.js"></script>
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
<body style="background:#000;">
<div id="container_monitor">
	<img src="<%=FASTCAT_MANAGE_ROOT%>images/fastcatsearch-logo.gif" />
	<hr/>
<!-- header -->

<div id="sidebar_monitor">
		
	<div class="sidebox">
		<h3>모니터링 설정</h3>
		<ul class="latest">
			<li>사이즈 : 
				<select id="sizeSelector" onchange="javascript:selectSize()">
				<option value="100">100%</option>
				<option value="90">90%</option>
				<option value="1240">1240</option>
				<option value="1024">1024</option>
				</select>
			</li>
			<li>모든정보 : <label for="monitering_switch"><input type="checkbox" id="main_switch" /> 활성화</label></li>
		</ul>
		<hr/>
		<br/>
		<h3>실시간 인기검색어 &nbsp;&nbsp;<label for="popular_switch"><input type="checkbox" id="popular_switch" /> 활성화</label></h3>
		<div class="fbox">
		<table class="tbl02">
		<thead>
		<tr>
		<th class="first">순위</th>
		<th>검색키워드</th>
		<th>변동</th>
		</tr>
		</thead>
			<tbody id="popular_keywords">
			
			</tbody>
		</table>
		</div>
		
		<h3>실시간 검색로그 &nbsp;&nbsp;<label for="log_switch"><input type="checkbox" id="log_switch" /> 활성화</label></h3>
		<div id="keyLogDiv" class="keyword_log">
		</div>
		
	</div>

</div><!-- E : #sidebar -->
		
<div id="mainContent_monitor">
		<h2>실시간 검색서버정보 &nbsp;&nbsp;<label for="monitering_switch"><input type="checkbox" id="monitering_switch" /> 활성화</label></h2>
		<div>
			<div style="float:left;width:33.3%;">
			<div id="chartJCPUDiv" style=" height:200px; background-color:#FFFFFF"></div>
			</div>
			<div style="float:left;width:33.3%;">
			<div id="chartMemDiv" style=" height:200px; background-color:#FFFFFF"></div>
			</div>
			<div style="float:left;width:33.3%;">
			<div id="chartLoadDiv" style=" height:200px; background-color:#FFFFFF"></div>
			</div>
		</div>
		<div>
			<div style="float:left;width:33.3%;">
			<div id="chartSearchActDiv" style=" height:200px; background-color:#FFFFFF"></div>
			</div>
			<div style="float:left;width:33.3%;">
			<div id="chartSearchTimeDiv" style=" height:200px; background-color:#FFFFFF"></div>
			</div>
			<div style="float:left;width:33.3%;">
			<div id="chartSearchFailDiv" style=" height:200px; background-color:#FFFFFF"></div>
			</div>
		</div>
		<p class="clear"></p>
		
		<h2>실시간 색인정보 &nbsp;&nbsp;<label for="indexing_switch"><input type="checkbox" id="indexing_switch" /> 활성화</label></h2>
		<div>
			<div style="float:left;width:33.3%;">
			<div id="chartFullIndexDiv" style=" height:250px; background-color:#FFFFFF"></div>
			</div>
			<div style="float:left;width:33.3%;">
			<div id="chartIncIndexDiv" style=" height:250px; background-color:#FFFFFF"></div>
			</div>
			<div style="float:left;width:33.3%;">
			<div id="chartIndexDocDiv" style=" height:250px; background-color:#FFFFFF"></div>
			</div>
		</div>
		<p class="clear"></p>
		
		<br/>
		<h2>최근이벤트내역 &nbsp;&nbsp;<label for="event_switch"><input type="checkbox" id="event_switch" /> 활성화</label></h2>
		<div class="fbox">
		<table summary="이벤트" class="tbl02">
		<colgroup><col width="80px" /><col width="80px" /><col width="70px" /><col width="*" /><col width="80px" /></colgroup>
		<thead>
		<tr>
			<th class="first">시간</th>
			<th>유형</th>
			<th>카테고리</th>
			<th>내용</th>
			<th>상태</th>
		</tr>
		</thead>
		<tbody id="searchEventDiv">
		</tbody>
		</table>
		</div>
		
		<div id="mjs:tip" class="tip" style="position:absolute;left:0;top:0;display:none;"></div>
		<div id="ver_monitor">FastcatSearch, Enterprise Search Engine</div>
<!-- E : #mainContent --></div>
</div><!-- //E : #container -->
</body>
</html>
