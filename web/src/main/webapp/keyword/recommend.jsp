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
<%@page import="org.fastcatsearch.datasource.DataSourceSetting"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="org.fastcatsearch.db.DBService" %>
<%@page import="org.fastcatsearch.db.vo.*" %>
<%@page import="org.fastcatsearch.db.dao.*" %>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.net.URLDecoder"%>
<%@include file="../common.jsp" %>

<%
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	String keyword = URLDecoder.decode(WebUtils.getString(request.getParameter("keyword"),""),"utf-8");
	String encodedKeyword = "";
	encodedKeyword = URLEncoder.encode(URLEncoder.encode(keyword, "utf-8"),"utf-8");
	
	String searchWord = "";
	boolean isSearch = false;
	if(!(keyword.equals(""))) {
		isSearch = true;
	}

	int pageNo = WebUtils.getInt(request.getParameter("pageNo"), 1);
	
	String dictionaryId = "RecommendKeyword";
	
	int pageSize = 20;
	int startRow = (pageNo - 1) * pageSize;
	int recordCount=0;
	
	List<SetDictionaryVO> result = null;
	
	SetDictionary recommendKeyword = dbHandler.getDAO(dictionaryId);
	if(recommendKeyword == null){
		out.println(dictionaryId+"를 찾을수 없습니다.");
		return;	
	}
	
	if(!"".equals(keyword)) {
		result = recommendKeyword.selectPageWithKeyword(keyword,startRow,pageSize);
		recordCount = recommendKeyword.selectCountWithKeyword(keyword);
	}else{
		keyword = "";
		result = recommendKeyword.selectPage(startRow,pageSize);
		recordCount = recommendKeyword.selectCount();
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
function clearRecommendDic(){
	if(confirm("모든 단어를 삭제하시겠습니까?")){
		submitPost('keywordService.jsp', {cmd: 3, target: 'recommend' });
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
	         submitPost('<%=request.getRequestURI() %>');
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
	<div class="sidebox">
		<h3>검색어관리</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/recommend.jsp" class="selected">추천어</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/popularKeyword.jsp">인기검색어</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/popularFailKeyword.jsp">실패검색어</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>keyword/hourlyKeyword.jsp">시간대별검색어</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			입력된 검색어를 keyword값으로 전달하면 json형식의 결과가 리턴된다.
			</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
	<h2>추천어관리</h2>
	
	<div id="conLbox.del">
		<div class="searchBox">
		<div class="box">
		<form action="recommend.jsp" method="post">
			<input type="text" class="inp02" name="keyword" size="42" maxlength="20" title="입력단어의 길이는 20자이내입니다."/>
			<input type="submit" value="찾기" class="btn_c" />
		</form>
		</div>
		
		<ul class="list_top">
			<li class="fl">총 <span class="tp2"><%=recordCount%></span>개 <%=(recordCount > 0)? "중 "+pageNo+"페이지" : "" %></li>
			<li class="fl"><%
				if(isSearch){
			%><span class="bullet">검색어 : </span><span class="tp"><%=keyword%></span> <a href="javascript:submitPost('<%=request.getRequestURI() %>')">전체보기</a><%
 	}
 %></li>
		</ul>
		</div>
		
		
		<form action="keywordService.jsp" method="post" name="delete" id="delete">
		<input type="hidden" name="cmd" value="12" />
		<div class="fbox">
		<table summary="작업주기설정" class="tbl02">
		<colgroup><col width="5%" /><col width="20%" /><col width="" /></colgroup>
		<thead>
		<tr>
			<th class="first">선택</th>
			<th>대표단어</th>
			<th>엔트리</th>
			</tr>
		</thead>
		<tbody>
		
<%
			int idx = recordCount - (pageSize * (pageNo - 1));

			for(int i = 0; i < result.size(); i++){
				SetDictionaryVO vo = result.get(i);
				if ( vo == null ){
					continue;
				}
				String[] wordList = vo.keyword.split(",");
				int count = wordList.length;
				String representative = "";
				StringBuilder sb = new StringBuilder();
				for(String word : wordList){
					
					if(word.startsWith("@")){
						representative = word.substring(1);
					}else{
						if(sb.length() > 0){
							sb.append(",");
						}
						sb.append(word);
					}
				}
				
		%>
	<tr>
		<td class="first"><input type="checkbox" name="checkGroup" value="<%=vo.id%>" /></td>
		<td>
		<strong class="small tb"><span id="dickey<%=vo.id%>"><%=representative%></span></strong></td>
		<td class="left"><span id="value<%=vo.id%>"><%=sb.toString() %></span></td>
	</tr>
<%
	}
%>
		</tbody>
		</table>
		</div>
		</form>
		
		<div class="list_bottom">
			<% String category = ""; //페이지네비게이션에서 사용하는 더미 밸류.%>
			<%@include file="../pageNavigation.jspf" %>
			<div style="width:100%">
				<span style="float:left">
					<input type="button" value="선택삭제" onclick="javascript:deleteRecommendWord();" class="btn_del" style="float:left"/>
					<input type="button" value="초기화" class="btn_init" onclick="clearRecommendDic();" style="float:left"/>
				</span>
				<p class="addbox">
					<input type="text" class="inp02" id="recommendKey" tabindex="1" size="15" />
					<input type="text" class="inp02" id="recommendValue" tabindex="2" size="40" onkeydown="javascript:addRecommendWord(event);"/>
					<input type="button" value="새로추가" class="btn_c" onclick="addRecommendWord();"/>
				</p>
				<p class="clear"></p>
			</div>
			
			<form id="file_upload" name="file_upload" action="fileInsertService.jsp" method="post" enctype="multipart/form-data">
				<input type="hidden" name="cmd" value="0"/>
				<input type="file" name="file"/>
	 			<div style="font-size:8pt">파일로추가</div>
			</form>
			<table id="files" align="right" style="margin-right:10px"></table>
		</div>
	</div>
	
	<p class="clear"></p>
	
	<form action="keywordService.jsp" method="post" id="addwordForm">
		<input type="hidden" name="recommendWord" id="recommendWord"/>
		<input type="hidden" name="cmd" value="1" />
	</form>
	</div><!-- E : #mainContent -->

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	
</body>

</html>
