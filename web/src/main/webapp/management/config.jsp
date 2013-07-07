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
<%@page import="org.fastcatsearch.ir.config.DataSourceConfig"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.ir.config.FieldSetting"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>

<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLDecoder"%>

<%@include file="../common.jsp" %>

<%
	IRConfig irConfig = IRSettings.getConfig(true);
	String message = URLDecoder.decode(WebUtils.getString(request.getParameter("message"), ""),"utf-8");
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
<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.validate.min.js" type="text/javascript"></script>
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/validate.messages_ko.js" type="text/javascript"></script>
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/help.js" type="text/javascript"></script>
	<script type="text/javascript">
	
	$(document).ready(function() {
		$("#configForm").validate({
			errorClass : "invalidValue",
			rules: {
				"server.port" : {required: true, number: true, min: 8000, max: 12000}
			}
		});

		var message = "<%=message %>";
		if(message != "")
			alert(message);
		
	});

	function configSubmit(){
		if($("#configForm").valid()){
			$("#configForm").submit();
		}
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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/searchEvent.jsp">이벤트내역</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/jobHistory.jsp">작업히스토리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/account.jsp">계정관리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/config.jsp" class="selected">사용자설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/advConfig.jsp">고급설정보기</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/restore.jsp">복원</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/license.jsp">라이선스</a></li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
	
<form action="managementService.jsp" method="post" name="configForm" id="configForm">
<input type="hidden" name="cmd" value="1" />

	<h2>서버</h2>
	<div class="fbox">
	<table summary="서버" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>server.port</th>
		<td style="text-align:left;"><input type="text" name="server.port" id="server.port" value="<%=irConfig.getString("server.port") %>" size='20' maxlength='5' class='inp02 help' /></td>
		</tr>
	</tbody>
	</table>
	</div>
	
	<h2>사전</h2>
	<div class="fbox">
	<table summary="서버" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>유사어 사전 방향</th>
		<td style="text-align:left">
		&nbsp;<select name="synonym.two-way" id="synonym.two-way" class="help">
			<% 
			String temp = irConfig.getString("synonym.two-way");
			boolean isTwoWay = false;
			
			if(temp != null && temp.equalsIgnoreCase("true")){
				isTwoWay = true;
			}
			%>
			<option value="false" <%= (isTwoWay ? "":"selected") %>>단방향</option>
			<option value="true" <%= (isTwoWay ? "selected":"") %>>양방향</option>
		</select>
		</td>
		</tr>
	</tbody>
	</table>
	</div>
	
	<h2>동적클래스패스</h2>
	<div class="fbox">
	<table summary="서버" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>dynamic.classpath</th>
		<td><input type="text" name="dynamic.classpath" id="dynamic.classpath" value="<%=irConfig.getString("dynamic.classpath") %>" size='77' class='inp02 help' /></td>
		</tr>
	</tbody>
	</table>
	</div>

	<div id="btnBox">
	<a href="javascript:configSubmit()" class="btn">저장</a>
	</div>

</form>
	<!-- E : #mainContent --></div>

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	

</body>

</html>

