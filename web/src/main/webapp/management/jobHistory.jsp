<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="org.fastcatsearch.db.dao.JobHistory"%>
<%@page import="org.fastcatsearch.db.vo.*"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>

<%@include file="../common.jsp" %>

<%
	int pageNo = WebUtils.getInt(request.getParameter("pageNo"), 1);
	int pageSize = 10;
	
	int startRow = (pageNo - 1) * pageSize;
	
	int recordCount=0;
	int LastRec=0; 
	int LastStartRecord=0;
	
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
		<h3>관리</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/main.jsp">시스템상태</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/jobHistory.jsp">이벤트내역</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/jobHistory.jsp" class="selected">작업히스토리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/account.jsp">계정관리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/config.jsp">사용자설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/advConfig.jsp">고급설정보기</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/restore.jsp">복원</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/license.jsp">라이선스</a></li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
	<h2>작업 히스토리</h2>
	
	<div class="fbox">
	<table summary="컬렉션 정보" class="tbl02" width="920" style="table-layout:fixed; word-break:break-all;">
	<colgroup><col width="3%" /><col width="4%" /><col width="10%" /><col width="6%" /><col width="5%" /><col width="7%" /><col width="5%" /><col width="6%" /><col width="6%" /><col width="5%" /></colgroup>
	<thead>
	<tr>
		<th class="first">번호</th>
		<th>작업번호</th>
		<th>작업클래스명</th>
		<th>아규먼트</th>
		<th>성공여부</th>
		<th>결과내용</th>
		<th>스케쥴</th>
		<th>시작시각</th>
		<th>종료시각</th>
		<th>수행시간</th>
	</tr>
	</thead>
	<tbody>
<%
	JobHistory jobHistory = dbHandler.getDAO("JobHistory");
	List<JobHistoryVO> list = jobHistory.select(startRow, pageSize);

	recordCount = jobHistory.selectCount();
	
	for(int i = 0; i < list.size(); i++){
		JobHistoryVO jobHistoryVO = list.get(i);
		
		String startTimeStr = "";
		String durationStr = "";
%>
	<tr>
		<td class="first"><%=jobHistoryVO.id%></td>
		<td><%=jobHistoryVO.jobId%></td>
		<td><span title="<%=jobHistoryVO.jobClassName%>"><%=jobHistoryVO.jobClassName%></span></td>
		<td><span title="<%=jobHistoryVO.args%>"><%=jobHistoryVO.args%></span></td>
		<td><%=jobHistoryVO.isSuccess ? "성공" : "실행" %></td>
		<td><span title="<%=jobHistoryVO.resultStr%>"><%=(jobHistoryVO.resultStr.length() > 20) ? jobHistoryVO.resultStr.substring(0,19) : jobHistoryVO.resultStr%></span></td>
		<td><%=jobHistoryVO.isScheduled ? "자동" : "수동" %></td>
		<td><%=jobHistoryVO.startTime%></td>
		<td><%=jobHistoryVO.endTime%></td>
		<td><%=Formatter.getFormatTime((long)jobHistoryVO.duration)%></td>
	</tr>
<%
	}
	
	if(list.size() == 0){
	%>
		<tr><td colspan="10">작업 히스토리가 없습니다.</td></tr>
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
	    out.println("<span class='num'><a href='jobHistory.jsp?pageNo=1'>처음</a></span>");
	}else{
		out.println("<span class='btn'>처음</span>");
	}
	
    if(prevStart > 0){
    	out.println("<span class='num'><a href='jobHistory.jsp?pageNo="+prevStart+"'>이전</a></span>");
    }else{
    	out.println("<span class='btn'>이전</span>");
    }
	
	for(int c = counterStart; c < counterEnd; c++){
		if(c <= maxPage){
			if(c == pageNo){
				out.println("<span class='num'><a href='jobHistory.jsp?pageNo="+c+"' class='selected'>"+c+"</a></span>");
			}else{
				out.println("<span class='num'><a href='jobHistory.jsp?pageNo="+c+"'>"+c+"</a></span>");
			}
		}else{
			break;
		}
	}
	
	if(nextPage <= maxPage){
	    out.println("<span class='num'><a href=jobHistory.jsp?pageNo="+nextPage+">다음</a></span>");
	}else{
		out.println("<span class='btn'>다음</span>");
	}
	
	if(maxPage > 0){
		out.println("<span class='num'><a href=jobHistory.jsp?pageNo="+maxPage+">마지막</a></span>");
	}else{
		out.println("<span class='btn'>마지막</span>");
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
