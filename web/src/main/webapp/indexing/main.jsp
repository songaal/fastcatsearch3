<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.config.Schema"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.datasource.DataSourceSetting"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.db.dao.IndexingResult"%>
<%@page import="org.fastcatsearch.ir.search.SegmentInfo"%>
<%@page import="org.apache.commons.io.FileUtils"%>
<%@page import="org.fastcatsearch.service.*"%>

<%@page import="java.io.File"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>

<%@include file="../common.jsp" %>

<%
	IRService irService = ServiceManager.getInstance().getService(IRService.class);
	//String[] colNames = irService.getCollectionNames();
	IRConfig irConfig = IRSettings.getConfig(true);
	String collectinListStr = irConfig.getString("collection.list");
	String[] colletionList = collectinListStr.split(",");
	
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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/main.jsp" class="selected">색인정보</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/result.jsp">색인결과</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/schedule.jsp">작업주기설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/history.jsp">색인히스토리</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>색인데이터에 대한 정보입니다.</li>
			<li>시퀀스 : 색인파일데이터 번호</li>
			<li>세그먼트 : 부분 색인데이터</li>
			<li>리비전 : 수정된 색인데이터의 리비전번호 </li>
			<li>베이스번호 : 세그먼트의 시작문서번호</li>
			<li>문서갯수 : 세그먼트에 담겨있는 문서갯수</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->


<div id="mainContent">
	<h2>색인정보</h2>
	<div class="fbox">
	<table summary="색인정보" class="tbl02">
	<colgroup><col width="3%" /><col width="7%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="8%" /><col width="8%" /><col width="10%" /></colgroup>
	<thead>
	<tr>
		<th class="first">No.</th>
		<th>컬렉션</th>
		<th>시퀀스</th>
		<th>세그먼트</th>
		<th>리비전</th>
		<th>베이스번호</th>
		<th>문서갯수</th>
		<th>색인파일크기</th>
		<!-- th>세그먼트경로</th-->
		<th>생성시각</th>
	</tr>
	</thead>
	<tbody>
<%
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	for(int i = 0; i < colletionList.length; i++){
		String collection = colletionList[i];
		CollectionHandler collectionHandler = irService.collectionHandler(collection);
		int dataSequence = -1;
		int segmentSize = 0;
		if(collectionHandler != null){
	dataSequence = collectionHandler.getDataSequence();
	segmentSize = collectionHandler.segmentSize();
		}
		if(!("".equals(collection) && colletionList.length==1)) {
		if(segmentSize > 0){
%>
		<tr>
		<td rowspan="<%=segmentSize %>" class="first"><%=i+1 %></td>
		<td rowspan="<%=segmentSize %>"><strong class="small tb"><%=collection %></strong></td>
		<td rowspan="<%=segmentSize %>"><%=dataSequence %></td>
		<%
		for(int k = 0; k < segmentSize; k++){
			if(k > 0){
				out.println("<tr>");
			}
			SegmentInfo segInfo = collectionHandler.getSegmentInfo(k);
			%>
			<% if(segInfo == null){ %>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<% }else{ %>
			<td><%=segInfo.getSegmentNumber() %></td>
			<td><%=segInfo.getLastRevision() %></td>
			<td><%=segInfo.getBaseDocNo() %></td>
			<td><%=segInfo.getDocCount() %></td>
			<td><%=Formatter.getFormatSize(FileUtils.sizeOfDirectory(segInfo.getSegmentDir())) %></td>
			<!-- td><%=segInfo.getSegmentDir().getAbsolutePath() %></td-->
			<td><%=sdf.format(new Date(segInfo.getTime())) %></td>
			</tr>
			<% } %>
			<%
		}
		} else {
			%>
			<tr>
			<td class="first"><%=i+1 %></td>
			
			<td><strong class="small tb"><%=collection %></strong></td>
			<td>-</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<!-- td>&nbsp;</td-->
			<td>&nbsp;</td>
			</tr>
			<%
		}
		} else {
%>
			<tr>
				<td colspan="9">컬렉션이 존재하지 않습니다.</td>
			</tr>
<%
		}
		
	}
%>
	</tbody>
	</table>
	</div>
	
	<!-- E : #mainContent --></div>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	
</body>

</html>
