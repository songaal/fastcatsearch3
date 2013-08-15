<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.db.dao.IndexingResult"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="org.fastcatsearch.db.dao.IndexingHistory"%>
<%@page import="org.fastcatsearch.db.vo.*"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.service.*"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>

<%@include file="../common.jsp" %>

<%
	int pageNo = WebUtils.getInt(request.getParameter("pageNo"), 1);
	int pageSize = 10;
	
	int startRow = (pageNo - 1) * pageSize;
	 
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
		<h3>색인</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/main.jsp">색인정보</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/result.jsp">색인결과</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/schedule.jsp">작업주기설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/history.jsp" class="selected">색인히스토리</a></li>
			</ul>
	</div>
</div><!-- E : #sidebar -->


<div id="mainContent">
	<h2>색인히스토리</h2>
	<div class="fbox">
	<table summary="색인히스토리" class="tbl02">
	<thead>
	<tr>
		<th class="first">번호</th>
		<th>컬렉션명</th>
		<th>색인타입</th>
		<th>성공여부</th>
		<th>추가문서수</th>
		<th>업데이트수</th>
		<th>삭제문서수</th>
		<th>스케쥴링</th>
		<th>시작시각</th>
		<th>종료시각</th>
		<th>실행시간</th>
	</tr>
	</thead>
	<tbody>
<%
	IndexingHistory indexingHistory = dbHandler.getDAO("IndexingHistory");
	List<IndexingHistoryVO> list = indexingHistory.select(startRow, pageSize);

	int recordCount = indexingHistory.count();

	for(int i = 0; i < list.size(); i++){
		IndexingHistoryVO indexingHistoryVO = list.get(i);
		
%>
	<tr>
		<td class="first"><%=indexingHistoryVO.id%></td>
		<td><strong class="small tb"><%=indexingHistoryVO.collection%></strong></td>
		<td><%

			if(indexingHistoryVO.type.equals("FULL")){
				out.println("전체색인");
			}else if(indexingHistoryVO.type.trim().equals("ADD")){
				out.println("증분색인");
			}
			%></td>
		<td><%=indexingHistoryVO.isSuccess ? "성공" : "실패" %></td>
		<td><%=indexingHistoryVO.docSize%></td>
		<td><%=indexingHistoryVO.updateSize%></td>
		<td><%=indexingHistoryVO.deleteSize%></td>
		<td><%=indexingHistoryVO.isScheduled ? "자동" : "수동" %></td>
		<td><%=indexingHistoryVO.startTime%></td>
		<td><%=indexingHistoryVO.endTime%></td>
		<td><%=Formatter.getFormatTime((long)indexingHistoryVO.duration)%></td>
	</tr>
<%
	}
	
	if(list.size() == 0){
	%>
		<tr><td colspan="11">색인 히스토리가 없습니다.</td></tr>
	<%
	}
%>
	</tbody>
	</table>
	</div>
	
<p class="clear"></p>
	<div class="list_bottom">
	<div id="paging">
	<%
	if(recordCount > 0){
		int counterWidth=5;
		int counterStart = ((pageNo - 1) / counterWidth) * counterWidth + 1;
		int counterEnd = counterStart + counterWidth; 
		int maxPage = 0;
		if(recordCount % pageSize == 0){
			maxPage = recordCount / pageSize;
		}else{
			maxPage = recordCount / pageSize + 1;
		}
		
		int prevStart = ((pageNo - 1) / counterWidth ) * counterWidth;
		int nextPage = ((pageNo - 1) / counterWidth  + 1) * counterWidth + 1;
		
		if(pageNo > counterWidth){
		    out.println("<span class='num'><a href='history.jsp?pageNo=1'>처음</a></span>");
		}else{
			out.println("<span class='btn'>처음</span>");
		}
		
	    if(prevStart > 0){
	    	out.println("<span class='num'><a href='history.jsp?pageNo="+prevStart+"'>이전</a></span>");
	    }else{
	    	out.println("<span class='btn'>이전</span>");
	    }
		
		for(int c = counterStart; c < counterEnd; c++){
			if(c <= maxPage){
				if(c == pageNo){
					out.println("<span class='num'><a href='history.jsp?pageNo="+c+"' class='selected'>"+c+"</a></span>");
				}else{
					out.println("<span class='num'><a href='history.jsp?pageNo="+c+"'>"+c+"</a></span>");
				}
			}else{
				break;
			}
		}
		
		if(nextPage <= maxPage){
		    out.println("<span class='num'><a href=history.jsp?pageNo="+nextPage+">다음</a></span>");
		}else{
			out.println("<span class='btn'>다음</span>");
		}
		
		if(maxPage > 0){
			out.println("<span class='num'><a href=history.jsp?pageNo="+maxPage+">마지막</a></span>");
		}else{
			out.println("<span class='btn'>마지막</span>");
		}
	}
	%>
	</div>
	</div>
	
	<!-- E : #mainContent --></div>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	
</body>

</html>
