<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.util.*"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.io.CharVector" %>
<%@page import="org.fastcatsearch.ir.analysis.Tokenizer"%>
<%@page import="org.fastcatsearch.plugin.*"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.apache.lucene.analysis.*"%>
<%@page import="java.util.*"%>

<%@include file="../common.jsp" %>

<%
/*
	AnalyzerServlet을 호출하여 결과를 가져온다.
*/
	PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
	
	List<String> analyzerNames = new ArrayList<String>();
	List<String> analyzerClasses = new ArrayList<String>();
	
	for(Plugin plugin : pluginService.getPlugins()){
		if(plugin instanceof AnalysisPlugin){
			PluginSetting pluginSetting = plugin.getPluginSetting();
			List<PluginSetting.Analyzer> list = pluginSetting.getAnalyzerList();
			for(PluginSetting.Analyzer analyzer :list){
				analyzerNames.add(analyzer.getName());
				analyzerClasses.add(analyzer.getValue());
			}
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
		
		function doAnalyze(){
			if($("#analyzerClass").val() == "") {
				alert("분석기를 선택해주세요.");
				return;
			}
			$.ajax({
				  url: location.protocol+"//"+location.host+"/analyzer/"+$("#analyzerClass").val(),
				  data: {
					keyword: $("#analyzeContents").val()
				  },
				  type: 'POST',
				  dataType: 'json',
				  error: function(XMLHttpRequest, textStatus, errorThrown) {
				  	 alert(errorThrown);
				  },
				  success: function(data_obj) {
					 $('#analysisResult').html("<ul/>");
					 $.each(data_obj.token, function(i, item){
					    $('#analysisResult ul').append('<li>'+item+'</li>');
					 });
				  }
			});
			
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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/analyzer.jsp" class="selected">분석기테스트</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/dbTester.jsp">DB테스트</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/searchDoc.jsp">문서원문조회</a></li>
			</ul>
	</div>
</div><!-- E : #sidebar -->


<div id="mainContent">

<h2>분석기 테스트</h2>
<div class="fbox">
<table summary="색인히스토리" class="tbl01">
<colgroup><col width="25%" /><col width="" /></colgroup>
<tbody>
	<tr>
		<th>분석기 선택</th>
		<td style="text-align:left">
		<select name="analyzer" id="analyzerClass">
			<option value="">==분석기선택==</option>
			<% 
			int k = 0;
			for(String analyzerName : analyzerNames){ 
				String clazz = analyzerClasses.get(k);
				k++;
			%>
			<option value="<%=clazz%>"><%=analyzerName%></option>
			<% } %>
		</select>
		</td>
	</tr>
	
	<tr>
		<th>분석내용</th>
		<td style="text-align:left"><textarea id="analyzeContents" name="contents" cols="80" rows="10"></textarea></td>
	</tr>
	<tr>
		<td colspan="2"><a href="javascript:doAnalyze()" class="btn">분석</a></td>
	</tr>
	
	<tr><th>분석결과</th><td style='text-align:left'><div id="analysisResult"></div></td></tr>
</tbody>
</table>
</div>

<!-- E : #mainContent --></div>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->

</body>

</html>
