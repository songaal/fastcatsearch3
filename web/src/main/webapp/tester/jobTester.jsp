<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="org.fastcatsearch.ir.config.*"%>
<%@page import="org.fastcatsearch.job.*"%>
<%@page import="org.fastcatsearch.control.*"%>
<%@page import="org.fastcatsearch.ir.io.CharVector" %>
<%@page import="org.fastcatsearch.ir.analysis.Tokenizer"%>
<%@page import="org.fastcatsearch.ir.IRService"%>

<%@include file="../common.jsp" %>

<%
	String jobName = request.getParameter("jobName");
	if(jobName == null)
		jobName = "";
	
	out.println("jobName = "+jobName);
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
	<script type="text/javascript">
	
		function selectTokenizer(dropdown){
			var myindex  = dropdown.selectedIndex
		    var selValue = dropdown.options[myindex].value
			location.href="?tokenizer="+selValue;
			return true;
		}

		function checkValues(myform){
			if(myform.contents.value == ''){
				return false;
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
		<h3>테스트</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/search.jsp">검색테스트</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/analyzer.jsp">분석기테스트</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/dbTester.jsp">DB테스트</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/searchDoc.jsp">문서원문조회</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/jobTester.jsp" class="selected">작업테스트</a></li>
			</ul>
	</div>
</div><!-- E : #sidebar -->


<div id="mainContent">

<h2>분석기 테스트</h2>
<form action="jobTester.jsp" method="post" onsubmit="javascript:return checkValues(this)" name="testerForm">
<div class="fbox">
<table summary="색인히스토리" class="tbl01">
<colgroup><col width="25%" /><col width="" /></colgroup>
<tbody>
	<tr>
		<th>작업선택</th>
		<td style="text-align:left">
			<input type="text" id="jobName" name="jobName" class='inp02' size="80" value="<%=jobName %>"/>
		</td>
	</tr>
	
	<tr>
		<th>파라미터</th>
		<td style="text-align:left"><textarea name="contents" cols="80" rows="3"></textarea></td>
	</tr>
	<tr>
		<td colspan="2"><a href="javascript:document.testerForm.submit()" class="btn">실행</a></td>
	</tr>
	<%
		if(jobName.length() > 0){
		out.println("<tr><th>결과</th><td style='text-align:left'>");
		Job job = (Job)IRClassLoader.getInstance().loadObject(jobName);
		ResultFuture jobResult = JobService.getInstance().offer(job);
		Object result = jobResult.take();
		out.println("result = "+result);
		out.println("<br>");
		out.println("</td></tr>");
			}
	%>
</tbody>
</table>
</div>
</form>

<!-- E : #mainContent --></div>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->

</body>

</html>
