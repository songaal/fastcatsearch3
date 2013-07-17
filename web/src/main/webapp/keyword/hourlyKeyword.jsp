<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.keyword.KeywordHit"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>

<%@include file="../common.jsp" %>
<%
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>FASTCAT 검색엔진 관리도구</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/reset.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style.css" rel="stylesheet" type="text/css" />
<!--[if lte IE 6]>
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style_ie.css" rel="stylesheet" type="text/css" />
<![endif]-->
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/common.js"></script>
</head>

<body>

<div id="container">
<!-- header -->
<%@include file="../header.jsp" %>

<div id="sidebar">
	<div class="sidebox">
		<h3>검색어관리</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/recommend.jsp">추천어</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/popularKeyword.jsp">인기검색어</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/popularFailKeyword.jsp">실패검색어</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/hourlyKeyword.jsp" class="selected">시간대별검색어</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>24시간동안 새로 입력된 키워드 들을 조회할 수 있습니다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
<h2>시간대별 검색어순위</h2>

<%
	for(int k=0; k<24; k++){
%>

<div class="fbox3 <%=(((k+1) % 3 == 0) ? "nomg":"") %>">
<h4><%=((k-1) > 0 ? k-1 : k+23) %> ~ <%=k %>시</h4>
<div class="fbox2_1">
<table summary="userDic" class="tbl02">
	<colgroup><col width="14%" /><col width="" /><col width="20%" /></colgroup>
	<thead>
	<tr>
		<th class="first">순위</th>
		<th>키워드</th>
		<th>검색수</th>
	</tr>
	</thead>
	<tbody>
<%
	List<KeywordHit> list = dbHandler.KeywordHit.selectKeywordHitLimit(0, k,10);

	for(int i = 0; i < list.size(); i++){
		KeywordHit keywordHit = list.get(i);
%>
	<tr>
		<td><%=i + 1%></td>
		<td><%=keywordHit.keyword%></td>
		<td><%=keywordHit.hit%></td>
	</tr>
<%
	}
	
	if(list.size() == 0){
	%>
		<tr><td colspan="3" class="first">인기검색어 내역이 없습니다.</td></tr>
	<%
	}
%>
	</tbody>
</table>
</div>
</div>
<%
	if((k+1) % 3 == 0){
		out.println("<p class=\"clear\"></p>");
	}
	
	}
%>

	<!-- E : #mainContent --></div>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->

</body>

</html>
