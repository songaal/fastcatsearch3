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
<%@page import="org.fastcatsearch.db.DBService" %>
<%@page import="org.fastcatsearch.db.dao.MapDictionary" %>
<%@page import="org.fastcatsearch.db.vo.*" %>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.net.URLDecoder"%>
<%@include file="../common.jsp" %>

<%
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);

	MapDictionary recommendKeyword = dbHandler.getDAO("RecommendKeyword", MapDictionary.class);
	
	String keyword = URLDecoder.decode(WebUtils.getString(request.getParameter("keyword"),""),"utf-8");
	String encodedKeyword = "";
	encodedKeyword = URLEncoder.encode(URLEncoder.encode(keyword, "utf-8"),"utf-8");
	
	String searchWord = "";
	boolean isSearch = false;
	if(!(keyword.equals(""))){
		isSearch = true;
	}

	int pageNo = WebUtils.getInt(request.getParameter("pageNo"), 1);
	
	int pageSize = 20;
	int startRow = (pageNo - 1) * pageSize;
	int recordCount=0;
	
	List<MapDictionaryVO> result = null;
	
	if(result == null){
		recordCount = recommendKeyword.selectCount();

		if(!"".equals(keyword)){
			result = recommendKeyword.selectPageWithKeyword(keyword,startRow,pageSize);
			recordCount = recommendKeyword.selectCountWithKeyword(keyword);
		}else{
			keyword = "";
			result = recommendKeyword.selectPage(startRow,pageSize);	
		}
		
	}else{
		recordCount = result.size();
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

<body onload="listen();">
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
			<li>추천어호출 URL은 다음과 같다.<br/>
			<a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/keyword/recommend?keyword=검색어" target="_blank">http://<%=request.getServerName()%>:<%=request.getServerPort()%>/keyword/recommend?keyword=검색어</a>
			<br/>입력된 검색어를 keyword값으로 전달하면 json형식의 결과가 리턴된다.
			</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
	<h2>추천어사전</h2>
	
	<div id="conLbox">
		<div class="searchBox">
		<div class="box">
		<form name="recommend.jsp" method="post">
			<input type="text" class="inp02" name="keyword" size="42" maxlength="10" title="입력단어의 길이는 10자이내입니다."/>
			<input type="submit" value="찾기" class="btn_c" />
		</form>
		</div>
		
		<ul class="list_top">
			<li class="fl">총 <span class="tp2"><%=recordCount%></span>개 <%=(recordCount > 0)? "중 "+pageNo+"페이지" : "" %></li>
			<li class="fl"><%
				if(isSearch){
			%><span class="bullet">검색어 : </span><span class="tp"><%=keyword%></span> <a href="recommend.jsp">전체보기</a><%
 	}
 %></li>
		</ul>
		</div>
		<div class="fbox">
		<table summary="작업주기설정" class="tbl02">
		<colgroup><col width="15%" /><col width="25%" /><col width="" /></colgroup>
		<thead>
		<tr>
			<th class="first">선택</th>
			<th>대표단어</th>
			<th width="140">엔트리</th>
			</tr>
		</thead>
		<tbody>
		
<%
			int idx = recordCount - (pageSize * (pageNo - 1));
			for(int i = 0; i < result.size(); i++){
				MapDictionaryVO sd = result.get(i);
		%>
	<tr>
		<td class="first"><%=idx--%></td>
		<td><a href="javascript:edit4Recommend('<%=sd.keyword%>', document.addForm.dicselect)">
		<strong class="small tb"><span id="dickey<%=sd.id%>"><%=sd.keyword%></span></strong></a></td>
		<td class="left"><span id="value<%=sd.id%>" title="<%=sd.value%>"><%=(sd.value.length() > 15) ? sd.value.substring(0,15)+".." : sd.value %></span></td>
	</tr>
<%
	}
%>
		</tbody>
		</table>
		</div>
		
		<div class="list_bottom">
			<div id="paging" class="fl">
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
				    out.println("<span class='num'><a href='recommendDic.jsp?pageNo=1'>처음</a></span>");
				}else{
					out.println("<span class='btn'>처음</span>");
				}
				
			    if(prevStart > 0){
			    	out.println("<span class='num'><a href='recommendDic.jsp?pageNo="+prevStart+"'>이전</a></span>");
			    }else{
			    	out.println("<span class='btn'>이전</span>");
			    }
				
				for(int c = counterStart; c < counterEnd; c++){
					if(c <= maxPage){
						if(c == pageNo){
							out.println("<span class='num'><a href='recommendDic.jsp?pageNo="+c+"' class='selected'>"+c+"</a></span>");
						}else{
							out.println("<span class='num'><a href='recommendDic.jsp?pageNo="+c+"'>"+c+"</a></span>");
						}
					}else{
						break;
					}
				}
				
				if(nextPage <= maxPage){
				    out.println("<span class='num'><a href=recommendDic.jsp?pageNo="+nextPage+">다음</a></span>");
				}else{
					out.println("<span class='btn'>다음</span>");
				}
				
				if(maxPage > 0){
					out.println("<span class='num'><a href=recommendDic.jsp?pageNo="+maxPage+">마지막</a></span>");
				}else{
					out.println("<span class='btn'>마지막</span>");
				}
			}
			%>
			</div>
			
			<p class="addbox">
				<input type="text" class="inp02" name="newwordvalue" id="newwordvalue" size="20" maxlength="10" title="입력단어의 길이는 10자이내입니다."/>
				<input type="button" value="새로추가" class="btn_c" onclick="addNewMajorWord();"/>
			</p>
			<p class="clear"></p>
			<input type="button" value="초기화" class="btn_init" onclick="clearRecommendDic()"/>
			<form id="file_upload" name="file_upload" action="fileInsertService.jsp" method="post" enctype="multipart/form-data">
				<input type="hidden" name="cmd" value="0"/>
				<input type="file" name="file"/>
	 			<div style="font-size:8pt">파일로추가</div>
			</form>
			<table id="files" align="right" style="margin-right:10px"></table>
		</div>
	</div>
	
	<div id="conRbox">
	<form id="addForm" name="addForm" method="post" action="keywordService.jsp">
		<input type="hidden" name="cmd" value="1" />
		<input type="hidden" name="newword" id="newword" />
		<input type="hidden" id="selectvalue" name="selectvalue" />
		<div class="box2"><em>대표단어</em><span id="editword" class="tword"></span></div>
		<p class="addbox">
			<input type="text" class="inp02" id="selectadd" name="selectadd" size="31" maxlength="10" title="입력단어의 길이는 10자이내입니다."/>
			<input type="text" class="inp02" id="dummy" name="dummy" style="display:none;" />
			<input type="button" value="추가" class="btn_c" onclick="addItem(document.addForm.dicselect)"/>
			<select name="dicselect" id="dicselect" multiple="multiple" size="16" class="select_m">
			</select>
		</p>
		<p class="btnbox">
		<!-- <a href="#" class="btn_up fl">▲</a> <a href="#" class="btn_down fl">▼</a> -->
		<input type="button" class="btn fr" value="전체삭제" onclick="deleteAll(document.addForm.dicselect);" title="리스트내 모든단어 삭제"/> 
		<input type="button" class="btn fr" value="삭제" onclick="deleteSelectItem(document.addForm.dicselect);" title="리스트내 선택단어 삭제"/>
		</p>
		<p class="box3">
		<input type="button" class="btn_b" value="저장" onclick="addNewWord(document.addForm.dicselect);"/> 
		<input type="button" class="btn_bd" value="삭제" onclick="deleteWordForRecommend();"/>
		</p>
	</form>
	</div>
	<p class="clear"></p>
	
	</div><!-- E : #mainContent -->

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	
</body>

</html>
