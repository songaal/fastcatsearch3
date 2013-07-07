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
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="org.fastcatsearch.db.DBService" %>
<%@page import="org.fastcatsearch.db.object.dic.BasicDictionary" %>
<%@page import="org.fastcatsearch.db.dao.SetDictionary" %>
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
	if(!(keyword.equals(""))) {
		isSearch = true;
	}

	int pageNo = WebUtils.getInt(request.getParameter("pageNo"), 1);
	
	int pageSize = 30;
	int startRow = (pageNo - 1) * pageSize;
	int recordCount=0;
	
	List<SetDictionary> result = null;

	recordCount = dbHandler.BasicDictionary.selectCount();

	if(!"".equals(keyword)){
		result = dbHandler.BasicDictionary.selectPageWithKeyword(keyword,startRow,pageSize);
		recordCount = dbHandler.BasicDictionary.selectCountWithKeyword(keyword);
	}else{
		keyword = "";
		result = dbHandler.BasicDictionary.selectPage(startRow,pageSize);	
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

function waitingDialog() { 
	$("#loadingScreen").html('진행중입니다. 시스템에 따라 30초에서 1분정도 소요될수 있습니다. ');
	$("#loadingScreen").dialog('option', 'title', '한국어사전 초기화');
	$("#loadingScreen").dialog('open');
}
function closeWaitingDialog() {
	$("#loadingScreen").dialog('close');
}

function bulkInsertKorean(){
	if(confirm("기존 한국어사전내용이 삭제되고, 초기화됩니다. 계속하시겠습니까?")){
		waitingDialog();
		$.ajax({
		  url: "dbservice.jsp?cmd=11",
		  timeout: 120000,//120초간 대기.
		  success: function(data){
				closeWaitingDialog();
				location.reload();
		 	if(data.trim() == 0){
				alert("초기화에 성공하였습니다.");
				location.reload();
		 	}else{
			 	alert("초기화에 실패했습니다. 시스템로그를 확인하세요.");
			 	location.reload();
			}
		  },
		  fail: function(XMLHttpRequest, textStatus, errorThrown) {
			  alert("초기화에 실패했습니다. 시스템로그를 확인하세요. "+errorThrown);
			  location.reload();
		  }
		  
		});
		
	}
}
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
			<li>검색시 추출되는 기본단어목록이며 조회용도로 사용한다.</li>
			<li>한국어사전은 편집이 불가능하며, 새로운 단어는 사용자사전을 이용한다.</li>
			<li>단어초기입력 : 검색엔진에 내장된 한국어사전파일이 관리도구로 입력된다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">

<h2>한국어사전</h2>
	<div class="searchBox">
	<div class="box">
	<form action="koreanDic.jsp" method="post">
		<input type="text" class="inp02" name="keyword" size="42" maxlength="20" title="입력단어의 길이는 20자이내입니다."/>
		<input type="submit" value="찾기" class="btn_c" />
	</form>
	</div>
	
	<ul class="list_top">
		<li class="fl">총 <span class="tp2"><%=recordCount%></span> 개</li>
		<li class="fl"><%
			if(isSearch){
		%><span class="bullet">검색어 : </span><span class="tp"><%=keyword%></span> <a href="koreanDic.jsp">전체보기</a><%
 	}
 %></li>
		<!-- <li class="fr"><input type="button" value="사전적용" class="btn_c bold" /></li> -->
	</ul>
	</div>
	
	<form action="dbservice.jsp" method="get" name="delete" id="delete">
	<input type="hidden" name="cmd" value="10" />
	
	<div class="fbox2">
	<table summary="userDic" class="tbl02">
	<colgroup><col width="14%" /><col width="25%" /><col width="" /></colgroup>
	<thead>
		<tr>
		<th colspan="3" class="first">단어</th>
		</tr>
	</thead>
	<tbody>
	<%
		int idx = recordCount - (pageSize * (pageNo - 1));
		if(result.size() > 0){
			int limit = (result.size() > 10) ? 10 : result.size();
			for(int i = 0; i < limit; i++){
		SetDictionary sd = result.get(i);
	%>
		<tr>
		<td class="first"><input type="checkbox" name="checkGroup" value="<%=sd.term%>" /></td>
			<td><%=idx--%></td>
			<td class="left"><strong class="small tb"><%=sd.term%></strong></td>
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
	<colgroup><col width="14%" /><col width="25%" /><col width="" /></colgroup>
	<thead>
	<tr>
		<th colspan="3" class="first">단어</th>
		</tr>
	</thead>
	<tbody>
	<%
		if(result.size() > 10){
			int limit = (result.size() > 20) ? 20 : result.size();
			for(int i = 10; i < limit; i++){
		SetDictionary sd = result.get(i);
	%>
		<tr>
		<td class="first"><input type="checkbox" name="checkGroup" value="<%=sd.term%>" /></td>
			<td><%=idx--%></td>
			<td class="left"><strong class="small tb"><%=sd.term%></strong></td>
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
	<colgroup><col width="14%" /><col width="25%" /><col width="" /></colgroup>
	<thead>
	<tr>
		<th colspan="3" class="first">단어</th>
		</tr>
	</thead>
	<tbody>
	<%
		if(result.size() > 20){
			int limit = result.size();
			for(int i = 20; i < result.size(); i++){
		SetDictionary sd = result.get(i);
	%>
		<tr>
		<td class="first"><input type="checkbox" name="checkGroup" value="<%=sd.term%>" /></td>
			<td><%=idx--%></td>
			<td class="left"><strong class="small tb"><%=sd.term%></strong></td>
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
		<%
		if(recordCount > 0){
			int counterWidth=5;
			int counterStart = ((pageNo - 1) / counterWidth) * counterWidth + 1;
			int counterEnd = counterStart + counterWidth; 
			int maxPage = 0;
			if(recordCount % pageSize == 0){
				maxPage = recordCount / pageSize;
			}else{
				maxPage = recordCount / pageSize + 1;
			}
			
			int prevStart = ((pageNo - 1) / counterWidth ) * counterWidth;
			int nextPage = ((pageNo - 1) / counterWidth  + 1) * counterWidth + 1;
			
			if(pageNo > counterWidth){
			    out.println("<span class='num'><a href='koreanDic.jsp?pageNo=1'>처음</a></span>");
			}else{
				out.println("<span class='btn'>처음</span>");
			}
			
		    if(prevStart > 0){
		    	out.println("<span class='num'><a href='koreanDic.jsp?pageNo="+prevStart+"'>이전</a></span>");
		    }else{
		    	out.println("<span class='btn'>이전</span>");
		    }
			
			for(int c = counterStart; c < counterEnd; c++){
				if(c <= maxPage){
					if(c == pageNo){
						out.println("<span class='num'><a href='koreanDic.jsp?pageNo="+c+"' class='selected'>"+c+"</a></span>");
					}else{
						out.println("<span class='num'><a href='koreanDic.jsp?pageNo="+c+"'>"+c+"</a></span>");
					}
				}else{
					break;
				}
			}
			
			if(nextPage <= maxPage){
			    out.println("<span class='num'><a href=koreanDic.jsp?pageNo="+nextPage+">다음</a></span>");
			}else{
				out.println("<span class='btn'>다음</span>");
			}
			
			if(maxPage > 0){
				out.println("<span class='num'><a href=koreanDic.jsp?pageNo="+maxPage+">마지막</a></span>");
			}else{
				out.println("<span class='btn'>마지막</span>");
			}
		}
		%>
		</div>
		
		
		<p class="addbox">
			<input type="button" value="단어 초기입력" onclick="javascript:bulkInsertKorean();" class="btn_c" />
		</p>
		<div id="loadingScreen"></div>

		<!--
		<p class="addbox">
			<input type="text" class="inp02" id="basicword" size="20" onkeydown="javascript:addBasicWord(event);" />
			<input type="submit" value="새로추가" onclick="javascript:addBasicWord();" class="btn_c"/>
			<input type="button" value="선택삭제" onclick="javascript:deleteBasicWord();" class="btn_c" />
		</p>
		
		<form action="dbservice.jsp" method="post" id="addwordForm">
			<input type="hidden" name="basicwordReal" id="basicwordReal"/>
			<input type="hidden" name="cmd" value="9" />
		</form>
		-->
	</div>
	<!-- E : #mainContent --></div>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	
</body>

</html>
