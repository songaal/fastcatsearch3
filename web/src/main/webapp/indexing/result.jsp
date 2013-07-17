<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.ir.settings.Schema"%>
<%@page import="org.fastcatsearch.ir.config.DataSourceConfig"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.db.dao.IndexingResult"%>
<%@page import="org.fastcatsearch.db.vo.*"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.fastcatsearch.ir.config.CollectionsConfig.*"%>

<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.net.URLDecoder"%>

<%@include file="../common.jsp" %>

<%
	String message = URLDecoder.decode(WebUtils.getString(request.getParameter("message"), ""),"utf-8");

	/* IRConfig irConfig = IRSettings.getConfig(true);
	String collectinListStr = irConfig.getString("collection.list");
	String[] colletionList = collectinListStr.split(","); */
	IRService irService = ServiceManager.getInstance().getService(IRService.class);
	List<Collection> collectionList = irService.getCollectionList();
	
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" />
<head>
<title>FASTCAT 검색엔진 관리도구</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/reset.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style.css" rel="stylesheet" type="text/css" />
<!--[if lte IE 6]>
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style_ie.css" rel="stylesheet" type="text/css" />
<![endif]-->
<script language="JavaScript" type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js"></script>
<script language="JavaScript" type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/common.js"></script>
<script language="JavaScript" type="text/javascript" >
	function alertMessage(){
		var message = "<%=message %>";
		if(message != "")
			alert(message);
	}
	
	
	function Indexing(collection, cmd){
			$.ajax({
				url:"indexingService.jsp",
				method:"post",
				data:{cmd:cmd,collection:collection},
				async : false,
					success:function(data) {
					data=data.replace(/^\s\s*/,"");
					alert(collection + " " + (cmd == "0" ? "전체" : "증분")+ " 색인을 수행 합니다.");
					$("#"+collection+"_"+cmd+"_status").html("색인중");
					$("#"+collection+"_"+cmd+"_docSize").html("");
					$("#"+collection+"_"+cmd+"_Auto").html("수동");
					$("#"+collection+"_"+cmd+"_Stime").html("");
					$("#"+collection+"_"+cmd+"_ETime").html("");
					$("#"+collection+"_"+cmd+"_DTime").html("");
					$("#"+collection+"_"+cmd+"_status").css("background","#00FF00").css("color","#FF0000");					
				}
			});


		}	
</script>
</head>

<body onload="alertMessage()">

<div id="container">
<!-- header -->
<%@include file="../header.jsp" %>

<div id="sidebar">
	<div class="sidebox">
		<h3>색인</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/main.jsp">색인정보</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/result.jsp" class="selected">색인결과</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/schedule.jsp">작업주기설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/history.jsp">색인히스토리</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>각 컬렉션에 대한 색인결과를 확인합니다.</li>
			<li>주기설정에 따라 자동으로 색인된 결과를 볼 수 있습니다.</li>
			<li>즉시 실행하고자 하면 색인버튼을 눌러 수동으로 실행할 수 있습니다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->


<div id="mainContent">
	<h2>색인결과</h2>
	<div class="fbox">
	<table summary="색인결과" class="tbl02">
	<colgroup><col width="3%" /><col width="10%" /><col width="10%" /><col width="6%" /><col width="8%" /><col width="8%" /><col width="10%" /><col width="10%" /><col width="7%" /><col width="7%" /></colgroup>
	<thead>
	<tr>
		<th class="first">No.</th>
		<th>컬렉션명</th>
		<th>색인타입</th>
		<th>성공여부</th>
		<th>문서수</th>
		<th>스케줄링</th>
		<th>시작시각</th>
		<th>종료시각</th>
		<th>실행시간</th>
		<th>실행</th>
	</tr>
	</thead>
	<tbody>
<%
	int i = 0;
	for(Collection col : collectionList){
		if(!col.isActive()){
			continue;
		}
		String collection = col.getId();
		
		IndexingResult indexingResult = dbHandler.getDAO("IndexingResult");
		IndexingResultVO fullIndexingResult = indexingResult.select(collection, "F");
		IndexingResultVO incIndexingResult = indexingResult.select(collection, "I");
		
		String startTimeStr = "";
		String durationStr = "";
%>
	<tr>
		<td rowspan="2" class="first"><%=i+1%></td>
		<td rowspan="2"><strong class="small tb"><%=collection%></strong></td>
		<td  >전체색인</td>
		<%
		if(fullIndexingResult != null){
		%>
		<td id="<%=collection%>_0_status"><%
		if(fullIndexingResult.status == IndexingResult.STATUS_SUCCESS){ 
			out.println("성공");
		}else if(fullIndexingResult.status == IndexingResult.STATUS_FAIL){ 
			out.println("실패");
		}else if(fullIndexingResult.status == IndexingResult.STATUS_RUNNING){ 
			out.println("색인중");
		}
		%></td>
		<td id="<%=collection%>_0_docSize" ><%=fullIndexingResult.docSize%></td>
		<td id="<%=collection%>_0_Auto" ><%=fullIndexingResult.isScheduled ? "자동" : "수동" %></td>
		<td id="<%=collection%>_0_Stime" ><%=fullIndexingResult.startTime%></td>
		<td id="<%=collection%>_0_ETime" ><%=fullIndexingResult.endTime%></td>
		<td id="<%=collection%>_0_DTime" ><%=Formatter.getFormatTime((long)fullIndexingResult.duration)%></td>
		<%
		}else{
		%>
		<td>-</td>
		<td>-</td>
		<td>-</td>
		<td>-</td>
		<td>-</td>
		<td>-</td>
		<%
		}
		%>
		<td><a href="javascript:Indexing('<%=collection%>','0');" class="btn_s">실행</a>
		<a href="javascript:Indexing('<%=collection%>','2');" class="btn_s">분산실행</a></td>
	</tr>
	<tr>
		<td>증분색인</td>
		<%
		if(incIndexingResult != null){
		%>
		<td id="<%=collection%>_1_status"><%
		if(incIndexingResult.status == IndexingResult.STATUS_SUCCESS){ 
			out.println("성공");
		}else if(incIndexingResult.status == IndexingResult.STATUS_FAIL){ 
			out.println("실패");
		}else if(incIndexingResult.status == IndexingResult.STATUS_RUNNING){ 
			out.println("색인중");
		}
		%></td>
		<td id="<%=collection%>_1_docSize"><%=incIndexingResult.docSize%></td>
		<td id="<%=collection%>_1_Auto"><%=incIndexingResult.isScheduled ? "자동" : "수동" %></td>
		<td id="<%=collection%>_1_Stime" ><%=incIndexingResult.startTime%></td>
		<td id="<%=collection%>_1_ETime"><%=incIndexingResult.endTime%></td>
		<td id="<%=collection%>_1_DTime"><%=Formatter.getFormatTime((long)incIndexingResult.duration)%></td>
		<%
		}else{
		%>
		<td>-</td>
		<td>-</td>
		<td>-</td>
		<td>-</td>
		<td>-</td>
		<td>-</td>
		<%
		}
		%>
		<td><a href="javascript:Indexing('<%=collection%>','1');" class="btn_s">실행</a></td>
	</tr>
<%
	
		i++;
	}
	
	
	if(i == 0){
%>
	<tr>
		<td colspan="10">
		컬렉션이 존재하지 않습니다.
		</td>
	</tr>
<%
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
