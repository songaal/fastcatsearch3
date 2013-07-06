<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="java.util.concurrent.ThreadPoolExecutor"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.Properties"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>

<%@include file="../common.jsp" %>

<%
	String message = "";
	String cmd = request.getParameter("cmd");
	if("apply".equals(cmd)){
		String currentPwd = request.getParameter("currentPwd");
		String newPwd = request.getParameter("newPwd");
		String newPwd2 = request.getParameter("newPwd2");
		if(newPwd.equalsIgnoreCase(newPwd2)){
			String username = (String)session.getAttribute("authorized");
			Object accessLog = IRSettings.isCorrectPasswd(username, currentPwd);
			
			if(accessLog != null){
				IRSettings.storePasswd(username, newPwd);
				message = "패스워드가 성공적으로 변경되었습니다.";
			}else{
				message = "현재 패스워드가 올바르지 않습니다.";
			}
		}else{
			message = "두 패스워드가 일치하지 않습니다.";
		}
		
	}

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
<script type="text/javascript">
	function applyPasswd(){
		<%
		if(!IRSettings.isAuthUsed()){
			%>
			alert("익명사용자는 이 기능을 사용할수없습니다.");
			return;
		<%
		}
		%>
		var form = document.accountForm;
		if(form.newPwd.value == '' || form.newPwd2.value == ''){
			alert("패스워드를 입력해주십시오.");
			return;
		}
		
		form.submit();
	}

	$(document).ready(function() {
		var message = "<%=message %>";
		if(message != "")
			alert(message);
	});
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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/account.jsp" class="selected">계정관리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/config.jsp">사용자설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/advConfig.jsp">고급설정보기</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/restore.jsp">복원</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/license.jsp">라이선스</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>익명사용자는 계정을 관리할 수 없습니다. 계정관리를 위해서는 계정사용을 활성화해주시기 바랍니다.</li>
			<li>활성화방법 : conf/auth파일내에 use=true로 셋팅</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
	<h2>계정관리</h2>
	<form action="" method="post" name="accountForm">
	<input type="hidden" name="cmd" value="apply" />
	<div class="fbox">
	<table summary="계정관리" class="tbl01">
	<colgroup><col width="23%" /><col width="" /></colgroup>
	<tbody>
	<tr>
		<th>현재 비밀번호</th>
		<td style="text-align:left"><input type="password" name="currentPwd" class="inp02" size="20" style="align:left"></input></td>
	</tr>
	<tr>
		<th>새 비밀번호</th>
		<td style="text-align:left"><input type="password" name="newPwd" class="inp02" size="20"></input></td>
	</tr>
	<tr>
		<th>새 비밀번호(확인)</th>
		<td style="text-align:left"><input type="password" name="newPwd2" class="inp02" size="20"></input></td>
	</tr>
	</tbody>
	</table>
	</div>
	
	<div id="btnBox">
	<a href="javascript:applyPasswd()" class="btn">수정</a>
	</div>
	</form>
	<!-- E : #mainContent --></div>

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	

</body>

</html>
