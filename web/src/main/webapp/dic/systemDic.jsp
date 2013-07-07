<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="org.fastcatsearch.service.ServiceManager"%>
<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.config.Schema"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.ir.dic.Dictionary"%>
<%@page import="org.fastcatsearch.ir.io.CharVector"%>
<%@page import="org.fastcatsearch.ir.config.DataSourceConfig"%>
<%@page import="org.fastcatsearch.plugin.Plugin"%>
<%@page import="org.fastcatsearch.plugin.AnalysisPlugin"%>
<%@page import="org.fastcatsearch.plugin.PluginService"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.List"%>
<%@page import="org.fastcatsearch.db.DBService" %>
<%@page import="org.fastcatsearch.db.vo.*" %>
<%@page import="org.fastcatsearch.db.dao.*" %>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.net.URLDecoder"%>
<%@include file="../common.jsp" %>

<%
	String keyword = URLDecoder.decode(WebUtils.getString(request.getParameter("keyword"),""),"utf-8");
	String encodedKeyword = "";
	encodedKeyword = URLEncoder.encode(URLEncoder.encode(keyword, "utf-8"),"utf-8");
	
	String searchWord = "";
	boolean isSearch = false;
	if(!(keyword.equals(""))) {
		isSearch = true;
	}

	int pageNo = WebUtils.getInt(request.getParameter("pageNo"), 1);
	String category = WebUtils.getString(request.getParameter("category"), "");
	
	int pageSize = 30;
	int startRow = (pageNo - 1) * pageSize;
	int recordCount=0;
	
	Dictionary<?> dictionary = null;
	{
		ServiceManager serviceManager = ServiceManager.getInstance();
		PluginService pluginService = serviceManager.getService(PluginService.class);
		Collection<Plugin> plugins = pluginService.getPlugins();
		for (Plugin plugin : plugins) {
			if (plugin.getPluginSetting().getNamespace().equals("Analysis")) {
				if(plugin.getPluginSetting().getId().equals(category)) {
					AnalysisPlugin analysisPlugin = (AnalysisPlugin)plugin;
					dictionary = analysisPlugin.getDictionary();
				}
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
<link href="<%=FASTCAT_MANAGE_ROOT%>css/jquery-ui.css" rel="stylesheet" />
<!--[if lte IE 6]>
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style_ie.css" rel="stylesheet" type="text/css" />
<![endif]-->

<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/common.js"></script>
<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js" type="text/javascript"></script>
<script src="<%=FASTCAT_MANAGE_ROOT%>js/dic.js" type="text/javascript"></script> 
<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-ui-1.8.9.min.js" type="text/javascript"></script>
<script type="text/javascript">

$(document).ready(function() {
	// create the loading window and set autoOpen to false
	$("#loadingScreen").dialog({
		autoOpen: false,	// set this to false so we can manually open it
		dialogClass: "loadingScreenWindow",
		closeOnEscape: false,
		draggable: false,
		width: 460,
		minHeight: 50,
		modal: true,
		buttons: {},
		resizable: false,
		open: function() {
			// scrollbar fix for IE
			$('body').css('overflow','hidden');
		},
		close: function() {
			// reset overflow
			$('body').css('overflow','auto');
		}
	}); // end of dialog
});


</script> 
<style>
#loadingScreen {
	padding-left: 15px;
}
/* 닫기버튼을 없앤다. */
.loadingScreenWindow .ui-dialog-titlebar-close {
	display: none;
}
</style>
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
			<li>검색시 추출되는 기본단어목록이며 단어유무 확인 용도로 사용한다.</li>
			<li>기본사전은 편집이 불가능하며 조회만 가능하다.</li>
			<li>새로운 단어는 사용자사전에 추가한다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">

<h2>기본사전</h2>
	<div class="searchBox">
	<div class="box">
	<form action="systemDic.jsp" method="post">
		<input type="hidden" name="category" value="<%=category%>"/>
		<input type="text" class="inp02" tabindex="1" name="keyword" size="42" maxlength="20" title="입력단어의 길이는 20자이내입니다."/>
		<input type="submit" value="찾기" class="btn_c" />
	</form>
	</div>
	
	<ul class="list_top">
		<% if(isSearch){ %>
		<%List result = dictionary.find(new CharVector(keyword)); %>
			<% if (result != null) { %>
			<li class="fl">총 <span class="tp2"><%=result.size()%></span> 개</li>
			<li class="fl">
			<span class="bullet">검색어 : </span><span class="tp"><%=keyword%></span>
				<div class="fbox">
					<table class="tbl02" style="width:700px;">
					<colgroup>
						<col width="30%"/>
						<col width="70%"/>
					</colgroup>
					<thead>
					<tr>
						<th class="first">키워드</th>
						<th>종류</th>
					</tr>
					</thead>
					<tbody>
					<% for(Object item : result) { %>
					<tr>
						<td class="first"><%=keyword %></td>
						<td><%=item %></td>
					</tr>
					<% } %>
					</tbody>
					</table>
				</div>
			</li>
			<% } else { %>
			<li class="fl">기본 사전 내 <span class="tp2">"<%=keyword%>"</span> 라는 단어가 발견되지 않았습니다.</li>
			<% } %>
		<% } %>
	</ul>
	</div>
	<!-- E : #mainContent --></div>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	
</body>

</html>