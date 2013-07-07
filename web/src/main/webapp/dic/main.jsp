<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="org.fastcatsearch.ir.dic.Dictionary"%>
<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.config.Schema"%>
<%@page import="org.fastcatsearch.ir.config.DataSourceConfig"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="org.fastcatsearch.db.DBService" %>
<%@page import="org.fastcatsearch.service.*" %>

<%@page import="org.fastcatsearch.db.dao.*" %>
<%@page import="org.fastcatsearch.db.vo.*" %>
<%@page import="org.fastcatsearch.plugin.*" %>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.io.File"%>
<%@include file="../common.jsp" %>

<%
	String category = WebUtils.getString(request.getParameter("category"), "");
	
	String synonymDictionaryId = category + "SynonymDictionary";
	String userDictionaryId = category + "UserDictionary";
	String stopDictionaryId = category + "StopDictionary";

	DBService dbService = ServiceManager.getInstance().getService(DBService.class);
	SetDictionary synonymDictionary = dbService.getDAO(synonymDictionaryId);
	SetDictionary userDictionary = dbService.getDAO(userDictionaryId);
	SetDictionary stopDictionary = dbService.getDAO(stopDictionaryId);
	Dictionary<?> systemDictionary = null;
	
	PluginService pluginService = ServiceManager.getInstance().getService(PluginService.class);
	Plugin plugin = pluginService.getPlugin(category);
	
	File synonymDictFile = null;
	File userDictFile = null;
	File stopDictFile = null;
	File systemDictFile = null;
	
	if(plugin != null){
		AnalysisPlugin analysisPlugin = (AnalysisPlugin) plugin;
		systemDictionary = analysisPlugin.getDictionary();
		File pluginDir = plugin.getPluginDir();
		String synonymDictPath = plugin.getPluginSetting().getProperties().get("synonym.dict.path");
		String userDictPath = plugin.getPluginSetting().getProperties().get("user.dict.path");
		String stopDictPath = plugin.getPluginSetting().getProperties().get("stop.dict.path");
		String systemDictPath = plugin.getPluginSetting().getProperties().get("system.dict.path");
		
		synonymDictFile = new File(pluginDir, synonymDictPath);
		userDictFile = new File(pluginDir, userDictPath);
		stopDictFile = new File(pluginDir, stopDictPath);
		systemDictFile = new File(pluginDir, systemDictPath);
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
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js" type="text/javascript"></script> 
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/dic.js" type="text/javascript"></script> 
	<script type="text/javascript">
	function doneSuccessApply(){
		alert("사전적용이 끝났습니다.");
		submitPost('main.jsp', {category: '<%=category%>'});
	}
	
	function doneFailApply(errorThrown){
		alert("사전적용을 시작하지 못했습니다. "+errorThrown);
	}
	</script>
</head>

<body>

<div id="container">
<!-- header -->
<%@include file="../header.jsp" %>

<div id="sidebar">
	<%@include file="submenu.jspf" %>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>사전정보를 확인하고 편집한 사전내용을 적용한다.</li>
			<li>파일크기 : 컴파일된 사전파일의 크기.</li>
			<li>메모리크기 : 메모리상에서 차지하는 사전데이터의 크기.</li>
			<li>엔트리갯수 : 컴파일된 사전내의 단어갯수</li>
			<li>적용버튼을 누르면 편집한 사전의 내용이 컴파일되어 검색엔진에 즉시 반영된다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
	<h2>사전정보</h2>
	<div class="fbox">
	<table summary="사전정보" class="tbl02">
	<colgroup><col width="10%" /><col width="30%" /><col width="20%" /><col width="30%" /><col width="10%" /></colgroup>
	<thead>
	<tr>
		<th class="first">번호</th>
		<th>사전명</th>
		<th>단어갯수</th>
		<th>적용시각</th>
		<th>적용</th>
	</tr>
	</thead>
	<tbody>
	<%
		int type = 1;
	%>
	<tr>
		<td class="first"><%=type %></td>
		<td><a href="javascript:void(0)" onclick="gotoDict('synonymDic.jsp', '<%=category%>')"><strong class="small tb">유사어사전</strong></a></td>
		<td><%=synonymDictionary.selectCount() %></td>
		<td><%=synonymDictFile != null ? new Timestamp(synonymDictFile.lastModified()) : "-" %></td>
		<td><a href="javascript:compileAndApplyDic('<%=category%>', 'synonymDict')" class="btn_s">사전적용</a></td>
	</tr>
	<%
		type = 2;
	%>
	<tr>
		<td class="first"><%=type %></td>
		<td><a href="javascript:void(0)" onclick="gotoDict('userDic.jsp', '<%=category%>')"><strong class="small tb">사용자사전</strong></a></td>
		<td><%=userDictionary.selectCount() %></td>
		<td><%=userDictFile != null ? new Timestamp(userDictFile.lastModified()) : "-" %></td>
		<td><a href="javascript:compileAndApplyDic('<%=category%>', 'userDict')" class="btn_s">사전적용</a></td>
	</tr>
	<%
		type = 3;
	%>
	<tr>
		<td class="first"><%=type %></td>
		<td><a href="javascript:void(0)" onclick="gotoDict('stopDic.jsp', '<%=category%>')"><strong class="small tb">금지어사전</strong></a></td>
		<td><%=stopDictionary.selectCount() %></td>
		<td><%=stopDictFile != null ? new Timestamp(stopDictFile.lastModified()) : "-" %></td>
		<td><a href="javascript:compileAndApplyDic('<%=category%>', 'stopDict')" class="btn_s">사전적용</a></td>
	</tr>
	<%
		type = 4;
	%>
	<tr>
		<td class="first"><%=type %></td>
		<td><a href="javascript:void(0)" onclick="gotoDict('systemDic.jsp', '<%=category%>')"><strong class="small tb">기본사전</strong></a></td>
		<td> <%=systemDictionary != null ? systemDictionary.size() : "-" %> </td>
		<td> - </td>
		<td> - </td>
	</tr>
	
	
	</tbody>
	</table>
	</div>
	
	<div style="float:right"><a href="javascript:compileAndApplyDic('<%=category%>', '')" class="btn_s">모든사전적용</a></div>
	<!-- E : #mainContent --></div>

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->

</body>

</html>
