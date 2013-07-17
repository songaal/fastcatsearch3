<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.service.*"%>

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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/popularKeyword.jsp" class="selected">인기검색어</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/popularFailKeyword.jsp" >실패검색어</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/hourlyKeyword.jsp">시간대별검색어</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>검색된 인기검색어를 관리합니다.</li>
			<li>키워드를 클릭하시면 키워드 편집이 가능합니다.</li>
			<li>"순위"의↑↓를 클릭함으로서 키워드 인기순위를 수동 조정할 수 있습니다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
<h2>인기검색어</h2>
<% List<KeywordHit> list = dbHandler.KeywordHit.selectPopular(); %>
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
				<th>인기율</th>
				<th>검색수</th>
				<th>순위</th>
				<th>활성화</th>
				<th>삭제</th>
				</tr>
			</thead>
			<tbody>
			<% int limit = (list.size() > COLUMN_SIZE * (k+1)) ? COLUMN_SIZE * (k+1) : list.size(); %>
			<% for(int i = COLUMN_SIZE * k; i < limit; i++){ %>
				<% KeywordHit keywordHit = list.get(i); %>
				<tr>
					<td><%=i + 1%></td>
					<td><a onclick="editKeyword(this)"><%=keywordHit.keyword%></a></td>
					<td><%=(keywordHit.popular / 100)%> %<input type="hidden" id="keywordHitPopular<%=i%>" value="<%=keywordHit.popular%>"/></td>
					<td><%=keywordHit.hit%></td>
					<td>
						<span
						><a style="cursor:pointer" onclick="changeKeywordRank(this,'<%=keywordHit.keyword %>',<%=i%>,1)">↑</a></span><span
						><a style="cursor:pointer" onclick="changeKeywordRank(this,'<%=keywordHit.keyword %>',<%=i%>,2)">↓</a></span><!--<span
						<%//FIXME:키워드랭크고정은 고려사항이 많으므로 다음번에 추가예정//%>
						<% if(keywordHit.prevRank==999) { %>
						></span><a href="javascript:{}">□</a><span
						<% } else { %>
						></span><a href="javascript:{}">■</a><span
						<% } %>
						></span>-->
					</td>
					<td><input type="checkbox" class="chk" <%=keywordHit.isUsed?"checked=\"true\"":""%> value="<%=URLEncoder.encode(keywordHit.keyword,"utf-8")%>" onclick="markUsed(this)"/></td>
					<td><input type="checkbox" class="chk" onclick="deleteKeyword(this)" value="<%=keywordHit.keyword%>"/></td>
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
		<th>인기율</th>
		<th>검색수</th>
		<th>순위</th>
		<th>활성화</th>
		<th>삭제</th>
		</tr>
	</thead>
	<tbody>
		<tr><td colspan="7" class="first">인기검색어 내역이 없습니다.</td></tr>
	</tbody>
	</table>
	</div>
<% } %>
<div class="searchBox">
	<div class="fbox4">
	<table summary="addKeyword" class="tbl02">
	<colgroup>
	<col width="" />
	<col width="20%" /> <col width="20%" /> <col width="20%" /></colgroup>
	<thead>
		<tr>
		<th class="first">키워드</th>
		<th>인기율</th>
		<th>검색수</th>
		<th>적용</th>
		</tr>
	</thead>
	<tbody>
		<tr>
		<td class="first"><input type="text" class="inp02" name="newkeyword" id="newkeyword" size="16" maxlength="10" title="입력단어의 길이는 10자이내입니다."/></td>
		<td><input type="text" class="inp02" name="newkeywordpop" id="newkeywordpop" size="3" maxlength="3" title="숫자만 입력하여 주십시요."/></td>
		<td><input type="text" class="inp02" name="newkeywordhit" id="newkeywordhit" size="3" maxlength="3" title="숫자만 입력하여 주십시요."/></td>
		<td><input type="button" value="추가" class="btn_c" onclick="addNewKeyWord();"/></td>
		</tr>
	</tbody>
	</table>
	</div>
</div>
<!-- E : #mainContent -->
</div>
<!-- footer -->
<%@include file="../footer.jsp" %>
</div><!-- //E : #container -->
</body>
</html>
