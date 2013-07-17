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
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="org.fastcatsearch.service.*" %>
<%@page import="org.fastcatsearch.db.DBService" %>
<%@page import="org.fastcatsearch.db.dao.*" %>
<%@page import="org.fastcatsearch.db.vo.*" %>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.net.URLDecoder"%>

<%@include file="../common.jsp" %>

<%
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	String keyword = URLDecoder.decode(URLDecoder.decode(WebUtils.getString(request.getParameter("keyword"),""),"utf-8"),"utf-8");
	String encodedKeyword = "";
	encodedKeyword = URLEncoder.encode(URLEncoder.encode(keyword, "utf-8"),"utf-8");
	
	String searchWord = "";
	boolean isSearch = false;
	if(!(keyword.equals(""))){
		isSearch = true;
	}

	int pageNo = WebUtils.getInt(request.getParameter("pageNo"), 1);
	String category = WebUtils.getString(request.getParameter("category"), "");
	String dictionaryId = category + "StopDictionary";
	int pageSize = 30;
	int startRow = (pageNo - 1) * pageSize;
	int recordCount=0;
	
	List<SetDictionaryVO> result = null;
	SetDictionary bannedDictionary = dbHandler.getDAO(dictionaryId);
	
	if(!"".equals(keyword)){
		result = bannedDictionary.selectPageWithKeyword(keyword,startRow,pageSize);
		recordCount = bannedDictionary.selectCountWithKeyword(keyword);
	}else{
		keyword = "";
		result = bannedDictionary.selectPage(startRow,pageSize);
		recordCount = bannedDictionary.selectCount();
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>FASTCAT 검색엔진 관리도구</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/reset.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/jquery.fileupload-ui.css" rel="stylesheet" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/jquery-ui.css" rel="stylesheet" />
<!--[if lte IE 6]>
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style_ie.css" rel="stylesheet" type="text/css" />
<![endif]-->
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/common.js"></script>
<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js" type="text/javascript"></script>
<script src="<%=FASTCAT_MANAGE_ROOT%>js/dic.js" type="text/javascript"></script> 
<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-ui-1.8.9.min.js" type="text/javascript"></script>
<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.fileupload.js" type="text/javascript"></script>
<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.fileupload-ui.js" type="text/javascript"></script> 
<script type="text/javascript"> 
function clearStopDic(){
	if(confirm("모든 단어를 삭제하시겠습니까?")){
		submitPost('dbservice.jsp', {cmd: 20, category:'<%=category %>', dic: 'stopDic' });
	}
}

$(function () {
	 $('#file_upload').fileUploadUI({
	     uploadTable: $('#files'),
	     buildUploadRow: function (files, index) {
	     	return $('<tr>' +
	                 '<td class="file_upload_progress"><div><\/div><\/td>' +
	                 '<td class="file_upload_cancel">' +
	                 '<button class="ui-state-default ui-corner-all" title="취소">' +
	                 '<span class="ui-icon ui-icon-cancel">취소<\/span>' +
	                 '<\/button><\/td><\/tr>');
	     },
	     parseResponse: function (xhr) {
	         if (typeof xhr.responseText !== 'undefined') {
	         	var resp = jQuery.trim(xhr.responseText);
	         	if(resp == 1){
		         	//alert("성공적으로 추가되었습니다.");
	         	}else{
		         	alert("추가 실패입니다. 파일의 형식에 오류가 있는지 확인해보세요.");
	         	}
	         } else {
	             // Instead of an XHR object, an iframe is used for legacy browsers:
	              var resp = jQuery.trim(xhr.contents().text());
	         	if(resp == 1){
	         		//alert("성공적으로 추가되었습니다.");
	         	}else{
	         		alert("추가 실패입니다. 파일의 형식에 오류가 있는지 확인해보세요.");
	         	}
	         }
	         submitPost('<%=request.getRequestURI() %>', {category:'<%=category %>'});
	     }
	 });
	});
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
			<li>검색을 원치않는 단어를 추가한다.</li>
			<li>새로추가버튼을 눌러 금지단어를 추가한다.</li>
			<li>파일로추가 : UTF-8로 인코딩의 텍스트파일을 선택하여 단어들을 일괄입력할 수 있다. 파일내부에는 한줄에 단어하나씩을 입력한다.</li>
			<li>초기화 : 저장된 모든 데이터를 지운다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">

<h2>금지어사전</h2>
	<div class="searchBox">
	<div class="box">
	<form action="stopDic.jsp" method="post">
		<input type="hidden" name="category" value="<%=category %>" />
		<input type="text" class="inp02" name="keyword" size="42" maxlength="20" title="입력단어의 길이는 20자이내입니다."/>
		<input type="submit" value="찾기" class="btn_c" />
	</form>
	</div>
	
	<ul class="list_top">
		<li class="fl">총 <span class="tp2"><%=recordCount%></span>개 <%=(recordCount > 0)? "중 "+pageNo+"페이지" : "" %></li>
		<li class="fl"><%
			if(isSearch){
		%><span class="bullet">검색어 : </span><span class="tp"><%=keyword%></span> <a href="javascript:submitPost('<%=request.getRequestURI() %>', {category:'<%=category %>'})">전체보기</a><%
 	}
 %></li>
		<!-- <li class="fr"><input type="button" value="사전적용" class="btn_c bold" /></li> -->
	</ul>
	</div>
	
	<form action="dbservice.jsp" method="post" name="delete" id="delete">
	<input type="hidden" name="cmd" value="7" />
	<input type="hidden" name="category" value="<%=category %>"/>
	
	<div class="fbox2">
	<table summary="userDic" class="tbl02">
	<colgroup><col width="14%" /><col width="" /></colgroup>
	<thead>
		<tr>
		<th colspan="2" class="first">단어</th>
		</tr>
	</thead>
	<tbody>
	<%
		int idx = recordCount - (pageSize * (pageNo - 1));
		if(result.size() > 0){
			int limit = (result.size() > 10) ? 10 : result.size();
			for(int i = 0; i < limit; i++){
				SetDictionaryVO sd = result.get(i);
	%>
		<tr>
		<td class="first"><input type="checkbox" name="checkGroup" value="<%=sd.keyword%>" /></td>
			<td class="left"><strong class="small tb"><%=sd.keyword%></strong></td>
		</tr>
	<%
		}
		}
	%>
	</tbody>
	</table>
	</div>
	
	<div class="fbox2">
	<table summary="userDic" class="tbl02">
	<colgroup><col width="14%" /><col width="" /></colgroup>
	<thead>
	<tr>
		<th colspan="2" class="first">단어</th>
		</tr>
	</thead>
	<tbody>
	<%
		if(result.size() > 10){
			int limit = (result.size() > 20) ? 20 : result.size();
			for(int i = 10; i < limit; i++){
				SetDictionaryVO sd = result.get(i);
	%>
		<tr>
		<td class="first"><input type="checkbox" name="checkGroup" value="<%=sd.keyword%>" /></td>
			<td class="left"><strong class="small tb"><%=sd.keyword%></strong></td>
		</tr>
	<%
		}
		}
	%>	
	</tbody>
	</table>
	</div>
		
	<div class="fbox2 nomg">
	<table summary="userDic" class="tbl02">
	<colgroup><col width="14%" /><col width="" /></colgroup>
	<thead>
	<tr>
		<th colspan="2" class="first">단어</th>
		</tr>
	</thead>
	<tbody>
	<%
		if(result.size() > 20){
			int limit = result.size();
			for(int i = 20; i < result.size(); i++){
				SetDictionaryVO sd = result.get(i);
	%>
		<tr>
		<td class="first"><input type="checkbox" name="checkGroup" value="<%=sd.keyword%>" /></td>
			<td class="left"><strong class="small tb"><%=sd.keyword%></strong></td>
		</tr>
	<%
		}
	}
	%>	
	</tbody>
	</table>
	</div>
	</form>
	
	<p class="clear"></p>
	<div class="list_bottom">
		<div id="paging">
		<%@include file="../pageNavigation.jspf" %>
		
		<div style="width:100%">
			<span style="float:left">
				<input type="button" value="선택삭제" onclick="javascript:deleteBannedWord();" class="btn_del" style="float:left"/>
				<input type="button" value="초기화" class="btn_init" onclick="clearStopDic();" style="float:left"/>
			</span>
			<p class="addbox">
				<input type="text" class="inp02" tabindex="1" id="bannedword" size="20" onkeydown="javascript:addBannedWord(event);" maxlength="20" title="입력단어의 길이는 20자이내입니다."/>
				<input type="button" value="새로추가" onclick="javascript:addBannedWord();" class="btn_c"/>
			</p>
		
			<p class="clear"></p>
			<form id="file_upload" name="file_upload" action="fileInsertService.jsp" method="post" enctype="multipart/form-data">
				<input type="hidden" name="cmd" value="1"/>
				<input type="hidden" name="category" value="<%=category%>"/>
				<input type="file" name="file"/>
	 			<div style="font-size:8pt">파일로추가</div>
		</form>
		</div>
		<table id="files" align="right" style="margin-right:10px"></table>
		
		<form action="dbservice.jsp" method="post" id="addBannedWordForm">
			<input type="hidden" name="bannedwordReal" id="bannedwordReal"/>
			<input type="hidden" name="category" value="<%=category %>"/>
			<input type="hidden" name="cmd" value="6" />
		</form>
	</div>
	<!-- E : #mainContent --></div>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	
</body>

</html>
