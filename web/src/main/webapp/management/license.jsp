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
<%@page import="org.fastcatsearch.ir.config.FieldSetting"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.ir.config.FieldSetting"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="com.fastcatsearch.license.*"%>

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
	function alertMessage(){
		var message = "<%=message %>";
		if(message != "")
			alert(message);
	}
	function verifyKey() {
		var param = { cmd:"4", key:$("#license\\.key").val() };
		$.ajax({
			url:"managementService.jsp",
			data:param, type:"POST", async:false,
			success:function(data) {
				data = data.replace(/^[ \n\r]*/g,"");
				data = data.replace(/[ \n\r]*$/g,"");
				if(data != "license ok") {
					alert("라이선스 키가 올바르지 않습니다.");
				} else {
					$("#configForm").submit();
				}
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
		<h3>관리</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/main.jsp">시스템상태</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/searchEvent.jsp">이벤트내역</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/jobHistory.jsp">작업히스토리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/account.jsp">계정관리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/config.jsp">사용자설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/advConfig.jsp">고급설정보기</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/restore.jsp">복원</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/license.jsp" class="selected">라이선스</a></li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
<%
LicenseInfo linfo = LicenseSettings.getInstance().getLicenseInfo();
%>
	
<form action="managementService.jsp" method="post" name="configForm" id="configForm">
<input type="hidden" name="cmd" value="5" />

	<h2>사용자정보</h2>
	<div class="fbox">
	<table summary="사용자정보" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<% if (linfo.getCompanyCode() != null) { %>
		<tr>
		<th>업체명</th>
		<td style="text-align:left;"><%=linfo.getCompanyName() %>(<%=linfo.getCompanyCode()%>)</td>
		</tr>
		<tr>
		<th>제품명</th>
		<td style="text-align:left;"><%=linfo.getProductName() %></td>
		</tr>
		<tr>
		<th>라이선스타입</th>
		<td style="text-align:left;"><%=linfo.getDisplayLicenseType() %></td>
		</tr>
		<tr>
		<th>서버ID</th>
		<td style="text-align:left;"><%=LicenseSettings.getServerId() %></td>
		</tr>
		<tr>
		<th>가용수량</th>
		<td style="text-align:left;"><%=linfo.getLicenseQuantity() %> EA</td>
		</tr>
		<tr>
		<th>발급일자</th>
		<td style="text-align:left;"><%=linfo.getDisplayCreateDate() %></td>
		</tr>
		<tr>
		<th>만료일자</th>
		<td style="text-align:left;"><%=linfo.getDisplayExpiredDate() %></td>
		</tr>
		<% } else { %>
		<tr>
			<th>서버ID</th>
			<td style="text-align:left;"><%=LicenseSettings.getServerId() %></td>
		</tr>
		<tr>
			<th>라이센스 정보</th>
			<td>라이센스 정보가 없습니다.</td>
		</tr>
		<% } %>
		<tr>
		<th>인증키</th>
		<td style="text-align:left;">
		<textarea name="license.key" id="license.key" style="width:400px;height:100px;" class='inp02 help'><%=(linfo.getLicenseKey()==null?"":linfo.getLicenseKey()) %></textarea></td>
		</tr>
	</tbody>
	</table>
	</div>

	<div id="btnBox">
	<a href="javascript:verifyKey()" class="btn">저장</a>
	</div>

</form>
	<!-- E : #mainContent --></div>

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	

</body>

</html>

