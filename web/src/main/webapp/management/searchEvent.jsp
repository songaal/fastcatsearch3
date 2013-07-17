<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="org.fastcatsearch.db.dao.SearchEvent"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.db.dao.JobHistory"%>
<%@page import="org.fastcatsearch.db.vo.*"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.fastcatsearch.log.EventDBLogger"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLDecoder"%>
<%@include file="../common.jsp" %>
<%
	String message = URLDecoder.decode(WebUtils.getString(request.getParameter("message"), ""),"utf-8");
	
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	int pageSize = 10;
	int startRow = 0; 
	int pageNo=0;
	
	int recordCount=0;
	int lastRec=0; 
	int lastStartRecord=0;
	
	if(request.getParameter("pageNo")==null){ 
	  if(startRow == 0){
	     pageNo = startRow + 1; 
	  }
	}else{
	  pageNo = Integer.parseInt(request.getParameter("pageNo")); 
	  startRow = (pageNo - 1) * pageSize; 
	}
	SearchEvent searchEvent = dbHandler.getDAO("SearchEvent");
	recordCount = searchEvent.selectCount();
	
	List<SearchEventVO> searchEventList = searchEvent.select(startRow+1,pageSize);
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
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js"></script>
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

<div id="sidebar">
	<div class="sidebox">
		<h3>관리</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/main.jsp">시스템상태</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/searchEvent.jsp" class="selected">이벤트내역</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/jobHistory.jsp">작업히스토리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/account.jsp">계정관리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/config.jsp">사용자설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/advConfig.jsp">고급설정보기</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/restore.jsp">복원</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/license.jsp">라이선스</a></li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
		<div style="width:735px;height:30px;">
			<h2>이벤트내역 상세</h2>
		</div>
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
		<tbody>
		<%
		if(searchEventList.size() == 0){
		%>
		<tr>
		<td colspan="5" class="first">이벤트내역이 없습니다.</td>
		</tr>	
		<%
		}else{
			for(int i=0;i<searchEventList.size();i++){
				SearchEventVO searchEventVO = searchEventList.get(i);	
				int id = searchEventVO.id;
				String time = searchEventVO.when.toString();
				time = time.substring(0, 19);
				String type = searchEventVO.type;
				String category = EventDBLogger.getCateName(searchEventVO.category);
				String summary = searchEventVO.summary;
				String status = searchEventVO.status;
				String stacktrace = searchEventVO.stacktrace;
		%>
		<tr>
		<td class="first"><%=time%></td>
		<td><strong class="small tb"><%=type%></strong></td>
		<td><%=category%></td>
		<td id="tips_<%=id%>" onclick="expandEvent(<%=id%>);" tips="<%=stacktrace%>"><%=summary%></td>
		<%
			if("T".equals(status)){
				status = "처리됨";
				%>
				<td id="td_<%=id%>"><%=status%></td>
				<% 
			}else{
				status = "미처리";
				%>
				<td id="td_<%=id%>"><a onclick="handleEvent(<%=id%>);" class="btn_s"><%=status%></a></td>
				<%
			}
		%>
		</tr>
		<%
			}//for
		}	
		%>
		</tbody>
		</table>
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
		    out.println("<span class='num'><a href='searchEvent.jsp?pageNo=1'>처음</a></span>");
		}else{
			out.println("<span class='btn'>처음</span>");
		}
		
	    if(prevStart > 0){
	    	out.println("<span class='num'><a href='searchEvent.jsp?pageNo="+prevStart+"'>이전</a></span>");
	    }else{
	    	out.println("<span class='btn'>이전</span>");
	    }
		
		for(int c = counterStart; c < counterEnd; c++){
			if(c <= maxPage){
				if(c == pageNo){
					out.println("<span class='num'><a href='searchEvent.jsp?pageNo="+c+"' class='selected'>"+c+"</a></span>");
				}else{
					out.println("<span class='num'><a href='searchEvent.jsp?pageNo="+c+"'>"+c+"</a></span>");
				}
			}else{
				break;
			}
		}
		
		if(nextPage <= maxPage){
		    out.println("<span class='num'><a href=searchEvent.jsp?pageNo="+nextPage+">다음</a></span>");
		}else{
			out.println("<span class='btn'>다음</span>");
		}
		
		if(maxPage > 0){
			out.println("<span class='num'><a href=searchEvent.jsp?pageNo="+maxPage+">마지막</a></span>");
		}else{
			out.println("<span class='btn'>마지막</span>");
		}
		%>
		</div>
		<div id="loadingScreen"></div>
		</div>
		<div id="mjs:tip" class="tip" style="position:absolute;left:0;top:0;display:none;"></div>
	</div>
<!-- E : #mainContent --></div>

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
</body>
</html>
