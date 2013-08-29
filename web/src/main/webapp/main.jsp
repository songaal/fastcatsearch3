<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="org.fastcatsearch.db.dao.SearchEvent"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="org.fastcatsearch.web.*"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.*"%>
<%@page import="org.fastcatsearch.db.dao.JobHistory"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.fastcatsearch.db.dao.IndexingSchedule"%>
<%@page import="org.fastcatsearch.db.vo.*"%>
<%@page import="org.fastcatsearch.ir.config.CollectionsConfig.*"%>
<%@page import="org.fastcatsearch.ir.config.*"%>


<%@page import="org.fastcatsearch.ir.util.Formatter"%>

<%@page import="org.apache.commons.io.FileUtils"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="java.util.Properties"%>

<%@include file="common.jsp" %>
<%
	String cmd = request.getParameter("cmd");
	String message = "";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>FASTCAT 검색엔진 관리도구</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/reset.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/amcharts/amcharts.js" ></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/amcharts/raphael.js" ></script>
<!--[if lte IE 6]>
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style_ie.css" rel="stylesheet" type="text/css" />
<![endif]-->

<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/common.js"></script>

<script>
	var popular_url = "/keyword/popular";

	$(document).ready(function() {
		var message = "<%=message%>";
		if(message != "")
			alert(message);

		$.ajax({
		    url: popular_url,
		    success: updatePopularKeyword,
		});
		
		
	});

	function logout(){
		location.href="?cmd=logout";
	}

	function updatePopularKeyword(data){
    	var popularList = "";
    	$.each(
			data.list,
			function(i, entity) {
				if(i > 4) return false;
				var term = entity.term;
				var preRank = entity.prevRank;
				var change = preRank - i - 1;
				if(change == 0)
					change = "-";
				if(preRank == 999)
					change = "new"
				
				popularList += '<tr><td class="first">'+(i+1)+'</td><td>'+term+'</td><td>'+change+'</td></tr>';
			}
		 );
		$("#popular_keywords").html(popularList);
    }
	
</script>
</head>
<body>
<div id="container">
<!-- header -->
<%@include file="header.jsp" %>

<div id="mainContent_home">
<%
	Properties systemProps = System.getProperties();
%>
		<div style="height:250px;">
			<div style="float:left; width:48%; background:#fff; margin: 5px 30px 5px 5px;">
			<h2>검색엔진 기본정보</h2>
			<div class="fbox">
			<table summary="기본정보" class="tbl01">
			<colgroup><col width="30%" /><col width="70%" /></colgroup>
			<tbody>
			<tr>
				<th class="first">검색엔진 버전</th>
				<td>FastcatSearch v2</td>
			</tr>
			<tr>
				<th class="first">검색엔진 HOME</th>
				<td><%=systemProps.getProperty("fastcatsearch.home")%></td>
			</tr>
			<tr>
				<th class="first">JDK벤더/버전</th>
				<td><%=systemProps.getProperty("java.vendor")%><br/>
				<%=systemProps.getProperty("java.vm.name")%><br/>
				<%=systemProps.getProperty("java.version")%></td>
			</tr>
			<tr>
				<th class="first">운영체제</th>
				<td><%=systemProps.getProperty("os.name")%> (<%=systemProps.getProperty("os.arch")%>) <%=systemProps.getProperty("os.version")%></td>
			</tr>
			<tr>
				<th class="first">사용계정</th>
				<td><%=systemProps.getProperty("user.name")%></td>
			</tr>
			</tbody>
			</table>
			</div>
			</div>
<%
	String koreanFileSize = "";
int koreanDicCount = 0;

String synonymFileSize = "";
int synonymDicCount = 0;

String userFileSize = "";
int userDicCount = 0;

String stopwordFileSize = "";
int stopwordDicCount = 0;

/* if(Dic.korean.file != null && Dic.korean.file.exists()){
	koreanFileSize = Formatter.getFormatSize(Dic.korean.file.length());
	koreanDicCount = Dic.korean.count();
}
if(Dic.synonym.file != null && Dic.synonym.file.exists()){
	synonymFileSize = Formatter.getFormatSize(Dic.synonym.file.length());
	synonymDicCount = Dic.synonym.count();
}
if(Dic.userword.file != null && Dic.userword.file.exists()){
	userFileSize = Formatter.getFormatSize(Dic.userword.file.length());
	userDicCount = Dic.userword.count();
}
if(Dic.stopword.file != null && Dic.stopword.file.exists()){
	stopwordFileSize = Formatter.getFormatSize(Dic.stopword.file.length());
	stopwordDicCount = Dic.stopword.count();
} */
%>
			<div style="float:left; width:46%; background:#fff; margin: 5px 5px 5px 5px;">
			<h2>서버정보</h2>
			<div class="fbox">
			<%-- <table summary="사전요약정보" class="tbl02">
			<colgroup><col width="30%" /><col width="30%" /><col width="40%" /></colgroup>
			<thead>
				<tr>
				<th class="first">사전명</th>
				<th>파일크기</th>
				<th>단어갯수</th>
				</tr>
			</thead>
			<tbody>
			<tr>
			<td class="first">유사어사전</td>
			<td><%=synonymFileSize%></td>
			<td><%=synonymDicCount%></td>
			</tr>
			<tr>
			<td class="first">사용자사전</td>
			<td><%=userFileSize%></td>
			<td><%=userDicCount%></td>
			</tr>
			<tr>
			<td class="first">금지어사전</td>
			<td><%=stopwordFileSize%></td>
			<td><%=stopwordDicCount%></td>
			</tr>
			<tr>
			<td class="first">기본사전</td>
			<td><%=koreanFileSize%></td>
			<td><%=koreanDicCount%></td>
			</tr>
			</tbody>
			</table> --%>
			</div>
			</div>
		</div>
		
<%
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
			DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);

				//IRService irService = ServiceManager.getInstance().getService(IRService.class);
				List<Collection> collectionList = irService.getCollectionList();
				//String[] colletionList = irService.getCollectionNames();
				int size = 0;
				String fullIndexingList = "";
				String incIndexingList = "";
				//DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);

				for(int i = 0; i < collectionList.size(); i++){
					String collection = collectionList.get(i).getId();
					CollectionHandler collectionHandler = irService.collectionHandler(collection);
					if(collectionHandler == null){
						continue;
					}
					CollectionContext collectionContext = collectionHandler.collectionContext();
					
					int dataSequence = -1;
					if(collectionHandler != null){
						dataSequence = collectionHandler.getDataSequence();
						File dataDir = collectionContext.collectionFilePaths().dataFile(dataSequence);
						if(dataDir != null && dataDir.exists()){
							size += FileUtils.sizeOfDirectory(dataDir);
						}
					}
					IndexingSchedule indexingSchedule = dbHandler.getDAO("IndexingSchedule");
					IndexingScheduleVO fullIndexingSchedule = indexingSchedule.select(collection, "F");
					IndexingScheduleVO incIndexingSchedule = indexingSchedule.select(collection, "I");
					if(fullIndexingSchedule != null && fullIndexingSchedule.isActive){
						fullIndexingList += collection + ", "; 
					}
					if(incIndexingSchedule != null && incIndexingSchedule.isActive){
						incIndexingList += collection + ", "; 
					}
				}

				fullIndexingList = fullIndexingList.trim();
				incIndexingList = incIndexingList.trim();

				if(fullIndexingList.length() > 0){
					fullIndexingList = fullIndexingList.substring(0, fullIndexingList.length() - 1);
				}else{
					fullIndexingList = "자동설정된 컬렉션이 없습니다.";
				}
				if(incIndexingList.length() > 0){
					incIndexingList = incIndexingList.substring(0, incIndexingList.length() - 1);
				}else{
					incIndexingList = "자동설정된 컬렉션이 없습니다.";
				}
		%>
		<div style="height:250px;">
			<div style="float:left;width:48%;height:250px; background:#fff; margin: 5px 30px 5px 5px;">
			<h2>색인요약정보</h2>
			<div class="fbox">
			<table summary="색인요약정보" class="tbl01">
			<colgroup><col width="30%" /><col width="70%" /></colgroup>
			<tbody>
			<tr>
				<th class="first">컬렉션갯수</th>
				<td><%=collectionList.size() %>개</td>
			</tr>
			<tr>
				<th class="first">색인파일 DISK 사용량</th>
				<td><%=Formatter.getFormatSize(size) %></td>
			</tr>
			<tr>
				<th class="first">자동 전체색인 컬렉션</th>
				<td><%=fullIndexingList %></td>
			</tr>
			<tr>
				<th class="first">자동 증분색인 컬렉션</th>
				<td><%=incIndexingList %></td>
			</tr>
			</tbody>
			</table>
			</div>
			</div>
			
			
<%
	List<SearchEventVO> searchEventList = dbHandler.getDAO("SearchEvent", SearchEvent.class).select(1, 5);

%>
			<div style="float:left;width:46%;height:250px; background:#fff; margin: 5px 5px 5px 5px;">
			<h2>최근이벤트내역</h2>
			<div class="fbox">
			<table summary="기본정보" class="tbl02">
			<colgroup><col width="30%" /><col width="10%" /><col width="60%" /></colgroup>
			<thead>
				<tr>
				<th class="first">발생시간</th>
				<th>구분</th>
				<th>내용</th>
				</tr>
			</thead>
			<tbody>
			<%
			if(searchEventList.size() == 0){
			%>
			<tr>
			<td colspan="3" class="first">이벤트내역이 없습니다.</td>
			</tr>	
			<%
			}else{
				for(int i=0;i<searchEventList.size();i++){
					SearchEventVO searchEvent = searchEventList.get(i);	
					int id = searchEvent.id;
					String time = searchEvent.when.toString();
					time = time.substring(0, 19);
					//String type = searchEvent.type;
					String category = "";//EventDBLogger.getCateName(searchEvent.category);
					String summary = searchEvent.summary;
					//String status = searchEvent.status;
					//String stacktrace = searchEvent.stacktrace;
			%>
			<tr>
			<td class="first"><%=time%></td>
			<td><%=category%></td>
			<td><%=summary%></td>
			</tr>
			<%
				}//for
			}	
			%>
			</tbody>
			</table>
			</div>
			</div>
		</div>

<!-- E : #mainContent --></div>

<!-- footer -->
<%@include file="footer.jsp" %>
	
</div><!-- //E : #container -->
</body>
</html>
