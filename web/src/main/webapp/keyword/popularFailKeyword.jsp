<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.keyword.KeywordHit"%>
<%@page import="org.fastcatsearch.keyword.KeywordFail"%>

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
<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js" type="text/javascript"></script> 
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/keyword.js"></script>
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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/popularKeyword.jsp" >인기검색어</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/popularFailKeyword.jsp" class="selected">실패검색어</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/hourlyKeyword.jsp">시간대별검색어</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>검색결과가 없거나 실패한 검색어를 관리합니다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
<h2>실패검색어</h2>
<% List<KeywordFail> list = dbHandler.KeywordFail.selectPopularFail(); %>
<% int COLUMN_SIZE = 33; %>
<% if(list.size() > 0){ %>
	<% //결과출력 %>
	<% for(int k=0; k<3; k++){ %>
		<% if(list.size() > COLUMN_SIZE * k){ %>
			<div class="fbox4" <%=((k == 2) ? "nomg":"") %>>
			<table summary="userDic" class="tbl02">
			<colgroup>
				<col width="9%" /> <col width="" /> <col width="15%" /> <col width="15%"/>
				<col width="15%"/> <col width="15%"/> <col width="9%"/>
			</colgroup>
			<thead>
				<tr>
				<th class="first">번호</th>
				<th>키워드</th>
				<th>실패율</th>
				<th>검색수</th>
				<th>순위</th>
				<th>활성화</th>
				<th>삭제</th>
				</tr>
			</thead>
			<tbody>
			<% int limit = (list.size() > COLUMN_SIZE * (k+1)) ? COLUMN_SIZE * (k+1) : list.size(); %>
			<% for(int i = COLUMN_SIZE * k; i < limit; i++){ %>
				<% KeywordFail keywordFail = list.get(i); %>
				<tr>
					<td><%=i + 1%></td>
					<td><a onclick="editKeyword(this)"><%=keywordFail.keyword%></a></td>
					<td><%=(keywordFail.popular / 100)%> %<input type="hidden" id="keywordHitPopular<%=i%>" value="<%=keywordFail.popular%>"/></td>
					<td><%=keywordFail.hit%></td>
					<td>
						<span
						><a style="cursor:pointer" onclick="changeKeywordRank(this,'<%=keywordFail.keyword %>',<%=i%>,1)">↑</a></span><span
						><a style="cursor:pointer" onclick="changeKeywordRank(this,'<%=keywordFail.keyword %>',<%=i%>,2)">↓</a></span><!--<span
						<%//FIXME:키워드랭크고정은 고려사항이 많으므로 다음번에 추가예정//%>
						<% if(keywordFail.prevRank==999) { %>
						></span><a href="javascript:{}">□</a><span
						<% } else { %>
						></span><a href="javascript:{}">■</a><span
						<% } %>
						></span>-->
					</td>
					<td><input type="checkbox" class="chk" <%=keywordFail.isUsed?"checked=\"true\"":""%> value="<%=URLEncoder.encode(keywordFail.keyword,"utf-8")%>" onclick="markUsed(this)"/></td>
					<td><input type="checkbox" class="chk" onclick="deleteKeyword(this)" value="<%=keywordFail.keyword%>"/></td>
				</tr>
			<% } %>
			</tbody>
			</table>
			</div>
		<% } %>
	<% } %>
<% } %>

<% if(list.size() == 0){ %>
	<% //결과없음. %>
	<div class="fbox4">
	<table summary="userDic" class="tbl02">
	<colgroup>
		<col width="9%" /> <col width="" /> <col width="15%" /> <col width="15%"/>
		<col width="15%"/> <col width="15%"/> <col width="9%"/>
	</colgroup>
	<thead>
		<tr>
		<th class="first">번호</th>
		<th>키워드</th>
		<th>실패율</th>
		<th>검색수</th>
		<th>순위</th>
		<th>활성화</th>
		<th>삭제</th>
		</tr>
	</thead>
	<tbody>
		<tr><td colspan="7" class="first">실패검색어 내역이 없습니다.</td></tr>
	</tbody>
	</table>
	</div>
<% } %>

<!-- E : #mainContent -->
</div>
<!-- footer -->
<%@include file="../footer.jsp" %>
</div><!-- //E : #container -->
</body>
</html>
