<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.settings.Schema"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.ir.config.DataSourceConfig"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="org.fastcatsearch.service.*"%>

<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.net.URLDecoder"%>

<%@include file="../common.jsp" %>

<%
	String message = URLDecoder.decode(WebUtils.getString(request.getParameter("message"), ""),"utf-8");

	IRService irService = ServiceManager.getInstance().getService(IRService.class);
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
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js" type="text/javascript"></script>
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.validate.min.js" type="text/javascript"></script>
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/validate.messages_ko.js" type="text/javascript"></script>
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/validate.additional.js" type="text/javascript"></script>
	<script type="text/javascript">
	$(document).ready(function() {
		$("#collectionForm").validate({
			errorClass : "invalidValue",
			wrapper : "li",
			errorLabelContainer: "#messageBox",
			submitHandler: function(form) {
				form.submit();
				return true;
			}
		});
	});
	
	function alertMessage(){
		var message = "<%=message%>";
		if(message != "")
			alert(message);
	}

	function addCollection(){
		$("#cmd").val("2");
		$("#collectionForm").submit();
	}

	function removeCollection(){
		var x = document.getElementsByName("selectCollection");
		collection = "";
		for(i=0;i<x.length;i++){
			if(x[i].checked){
				collection = x[i].value;
				break;
			}
		}

        if(collection == "")
            alert("삭제할 컬렉션을 선택해주세요.");
        else
			location.href = "collectionService.jsp?cmd=3&collection="+collection;
		
	}
	</script>
</head>

<body onload="alertMessage()">
<div id="container">
<!-- header -->
<%@include file="../header.jsp" %>

<div id="sidebar">
	<div class="sidebox">
		<h3>컬렉션</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>collection/main.jsp" class="selected">컬렉션정보</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>collection/schema.jsp">스키마설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>collection/datasource.jsp">데이터소스설정</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>컬렉션</h3>
			<ul class="latest">
			<li>각 컬렉션에 대한 정보입니다.</li>
			<li>데이터소스타입 : 수집파일로 부터 색인을 할 경우 FILE, DB로 데이터부터 색인을 할 경우 DB 선택.</li>
			<li>실행 : 각 컬렉션별로 서비스 여부를 선택할 수 있다.</li>
			<li>컬렉션을 추가할 때는 컬렉션이름을 기입하고 추가버튼을 누른다.</li>
			<li>컬렉션을 삭제할 때는 해당 컬렉션의 선택버튼을 클릭하고 삭제버튼을 누른다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
	<h2>컬렉션 정보</h2>
	<div class="fbox">
	<table summary="컬렉션 정보" class="tbl02">
	<colgroup><col width="5%" /><col width="5%" /><col width="10%" /><col width="8%" /><col width="8%" /><col width="8%" /><col width="8%" /><col width="8%" /><col width="8%" /><col width="7%" /><col width="8%" /><col width="8%" /></colgroup>
	<thead>
	<tr>
		<th rowspan = "2" class="first">선택</th>
		<th rowspan = "2">번호</th>
		<th rowspan="2">컬렉션명</th>
		<th colspan="5">필드갯수</th>
		<th rowspan="2">데이터<br />
			소스타입</th>
		<th rowspan="2">상태</th>
		<th rowspan="2">실행시간</th>
		<th rowspan="2">실행</th>
	</tr>
	<tr>
		<th>총필드수</th>
		<th>색인필드수</th>
		<th>필터필드수</th>
		<th>그룹필드수</th>
		<th>정렬필드수</th>
	</tr>
	</thead>
	<tbody>
	<%
		for(int i = 0;i<colletionList.length;i++){
		String collection = colletionList[i];
		CollectionHandler collectionHandler = irService.collectionHandler(collection);
		boolean isRunning = false;
		String startTimeStr = "";
		String durationStr = "";
		if(collectionHandler == null){
			isRunning = false;
		}else{
			isRunning = true;
			long startTime = collectionHandler.getStartedTime();
			long duration  = System.currentTimeMillis() - startTime;
			startTimeStr = new Date(startTime).toString();
			durationStr = Formatter.getFormatTime(duration);
		}
		
		Schema schema = IRSettings.getSchema(collection, true);
		
		if(schema!=null) {
			DataSourceConfig dataSourceSetting = IRSettings.getDatasource(collection, true);
			String sourceType = dataSourceSetting.sourceType;
	%>
	<tr>
		<td class="first"><input type="radio" name="selectCollection" value="<%=collection%>" /></td>
		<td><%=i+1 %></td>
		<td><a href="schema.jsp?collection=<%=collection%>"><strong class="small tb"><%=collection%></strong></a></td>
		<td><%=schema.getFieldSettingList().size()%></td>
		<td><%=schema.getIndexSettingList().size()%></td>
		<td><%=schema.getFilterSettingList().size()%></td>
		<td><%=schema.getGroupSettingList().size()%></td>
		<td><%=schema.getSortSettingList().size()%></td>
		<td><a href="datasource.jsp?collection=<%=collection%>"><%=sourceType%></a></td>
		<td><%=isRunning ? "실행중" : "정지"%></td>
		<td><%=durationStr%>&nbsp;</td>
		<td><%
		if(isRunning){
			%>
			<a class="btn_s" href="collectionService.jsp?cmd=0&collection=<%=collection%>">정지</a>
			<%
		}else{
			%>
			<a class="btn_s" href="collectionService.jsp?cmd=1&collection=<%=collection%>">시작</a>
			<%	
		}
		%>
		</td>
	</tr>
<%
		} else {
%>
	<tr>
		<td class="first" colspan="12" >컬렉션이 존재하지 않습니다.</td>
	</tr>
<%
		}
	}
%>
	</tbody>
	</table>
	</div>

	<div id="btnBox">
	<form id="collectionForm" method="get" action="collectionService.jsp">
	<input type="hidden" id="cmd" name="cmd" />
	<input type="text" id="collection" name="collection" class="inp02 required alphanumeric" size="20" minlength="2" maxlength="20"></input> 
	<a class="btn" onclick="javascript:addCollection()">컬렉션추가</a>
	<a class="btn" onclick="javascript:removeCollection()">컬렉션삭제</a>
	<div id="messageBox" style="width:400px; margin-left:220px; text-align: left;"> </div>
	</form>
	</div>
	
	<!-- E : #mainContent --></div>

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->

</body>

</html>
