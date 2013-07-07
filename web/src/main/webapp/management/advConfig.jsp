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
<%@page import="org.fastcatsearch.ir.config.FieldSetting"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>

<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLDecoder"%>

<%@include file="../common.jsp" %>

<%
	IRConfig irConfig = IRSettings.getConfig(true);
	String message = URLDecoder.decode(WebUtils.getString(request.getParameter("message"), ""),"utf-8");
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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/account.jsp">계정관리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/config.jsp">사용자설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/advConfig.jsp" class="selected">고급설정보기</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/restore.jsp">복원</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/license.jsp">라이선스</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>collection.list : 사용가능한 컬렉션리스트이다. fastcat.conf파일에서 수정가능하다.</li>
			<li>작업실행기 : 작업쓰레드 갯수를 설정한다.</li>
			<li>문서 : 문서저장소에 필요한 버킷사이즈, 버퍼사이즈, 블록사이즈를 설정한다.</li>
			<li>인덱싱 : 색인데이터 생성에 필요한 파라미터를 설정한다.</li>
			<li>search.highlightAndSummary : 검색결과 요약 및 키워드 하이라이팅을 수행하는 클래스. 관리자가 직접구현 사용가능.</li>
			<li>세그먼트 : 세그먼트데이터 생성에 필요한 파라미터를 설정한다.</li>
			<li>server.admin.path : 웹관리도구 war파일 경로</li>
			<li>server.port : 서비스에 사용할 검색엔진 서버 포트</li>
			<li>server.logs.dir : 로그를 저장할 디렉토리 경로.</li>
			<li>korean.dic.path : 한국어사전 경로</li>
			<li>user.dic.path : 유사어사전 경로</li>
			<li>유사어 사전 방향 : 단방향은 대표단어를 하위단어들로만 확장하는 반면, 양방향은 하위단어들을 대표단어로도 확장가능하다.</li>
			<li>stopword.dic.path : 금지어사전 경로</li>
			<li>specialCharacter.map.path : 정규화사전 경로</li>
			<li>dynamic.classpath : 관리자가 추가한 추가 jar파일등의 경로. 검색엔진Home의 상태경로이며, 여러개를 설정할 경우 경로구분자(: 또는 ;)로 구분한다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
	
<!-- <h3>※ 고급설정은 fastcat.conf에서 수정할 수 있습니다.</h3>
<br></br> -->
<input type="hidden" name="cmd" value="1" />
	<h2>컬렉션</h2>
	<div class="fbox">
	<table summary="컬렉션" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>collection.list</th>
		<td style="text-align:left"><%=irConfig.getString("collection.list") %>
		<input type="hidden" name="collection.list" value="<%=irConfig.getString("collection.list") %>" /></td>
		</tr>
	</tbody>
	</table>
	</div>
	
	<h2>작업실행기</h2>
	<div class="fbox">
	<table summary="작업실행기" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>jobExecutor.core.poolsize</th>
		<td style="text-align:left"><%=irConfig.getString("jobExecutor.core.poolsize") %></td>
		</tr>
		<tr>
		<th>jobExecutor.max.poolsize</th>
		<td style="text-align:left"><%=irConfig.getString("jobExecutor.max.poolsize") %></td>
		</tr>
		<tr>
		<th>jobExecutor.keepAliveTime</th>
		<td style="text-align:left"><%=irConfig.getString("jobExecutor.keepAliveTime") %></td>
		</tr>
	</tbody>
	</table>
	</div>
	
	<h2>문서</h2>
	<div class="fbox">
	<table summary="문서" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>pk.term.interval</th>
		<td style="text-align:left"><%=irConfig.getString("pk.term.interval") %></td>
		</tr>
		<tr>
		<th>pk.bucket.size</th>
		<td style="text-align:left"><%=irConfig.getString("pk.bucket.size") %></td>
		</tr>
		<tr>
		<th>document.read.buffer.size</th>
		<td style="text-align:left"><%=irConfig.getString("document.read.buffer.size") %></td>
		</tr>
		<tr>
		<th>document.write.buffer.size</th>
		<td style="text-align:left"><%=irConfig.getString("document.write.buffer.size") %></td>
		</tr>
		<tr>
		<th>document.block.size</th>
		<td style="text-align:left"><%=irConfig.getString("document.block.size") %></td>
		</tr>
		<tr>
		<th>document.compression.type</th>
		<td style="text-align:left"><%=irConfig.getString("document.compression.type") %></td>
		</tr>
	</tbody>
	</table>
	</div>
	
	<h2>인덱싱</h2>
	<div class="fbox">
	<table summary="인덱싱" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>index.term.interval</th>
		<td style="text-align:left"><%=irConfig.getString("index.term.interval") %></td>
		</tr>
		<tr>
		<th>index.work.bucket.size</th>
		<td style="text-align:left"><%=irConfig.getString("index.work.bucket.size") %></td>
		</tr>
		<tr>
		<th>index.work.memory</th>
		<td style="text-align:left"><%=irConfig.getString("index.work.memory") %></td>
		</tr>
		<tr>
		<th>index.work.check</th>
		<td style="text-align:left"><%=irConfig.getString("index.work.check") %></td>
		</tr>
		<tr>
		<th>data.sequence.cycle</th>
		<td style="text-align:left"><%=irConfig.getString("data.sequence.cycle") %></td>
		</tr>
	</tbody>
	</table>
	</div>

	<h2>검색</h2>
	<div class="fbox">
	<table summary="검색" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>search.highlightAndSummary</th>
		<td style="text-align:left"><%=irConfig.getString("search.highlightAndSummary") %></td>
		</tr>
	</tbody>
	</table>
	</div>

	<h2>세그먼트</h2>
	<div class="fbox">
	<table summary="세그먼트" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>segment.separate.add.indexing</th>
		<td style="text-align:left"><%=irConfig.getString("segment.separate.add.indexing") %></td>
		</tr>
		<tr>
		<th>segment.document.limit</th>
		<td style="text-align:left"><%=irConfig.getString("segment.document.limit") %></td>
		</tr>
		<tr>
		<th>segment.revision.backup.size</th>
		<td style="text-align:left"><%=irConfig.getString("segment.revision.backup.size") %></td>
		</tr>
	</tbody>
	</table>
	</div>

	<h2>서버</h2>
	<div class="fbox">
	<table summary="서버" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>server.admin.path</th>
		<td style="text-align:left"><%=irConfig.getString("server.admin.path") %></td>
		</tr>
		<tr>
		<th>server.port</th>
		<td style="text-align:left"><%=irConfig.getString("server.port") %></td>
		</tr>
		<tr>
		<th>server.logs.dir</th>
		<td style="text-align:left"><%=irConfig.getString("server.logs.dir") %></td>
		</tr>
	</tbody>
	</table>
	</div>
	
	<h2>사전</h2>
	<div class="fbox">
	<table summary="서버" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>korean.dic.path</th>
		<td style="text-align:left"><%=irConfig.getString("korean.dic.path") %></td>
		</tr>
		<tr>
		<th>user.dic.path</th>
		<td style="text-align:left"><%=irConfig.getString("user.dic.path") %></td>
		</tr>
		<tr>
		<th>synonym.dic.path</th>
		<td style="text-align:left"><%=irConfig.getString("synonym.dic.path") %></td>
		</tr>
		<tr>
		<th>synonym.two-way</th>
		<td style="text-align:left"><%=irConfig.getString("synonym.two-way").equalsIgnoreCase("true")?"양방향":"단방향" %></td>
		</tr>
		<tr>
		<th>stopword.dic.path</th>
		<td style="text-align:left"><%=irConfig.getString("stopword.dic.path") %></td>
		</tr>
		<tr>
		<th>specialCharacter.map.path</th>
		<td style="text-align:left"><%=irConfig.getString("specialCharacter.map.path") %></td>
		</tr>
	</tbody>
	</table>
	</div>

	<h2>동적클래스패스</h2>
	<div class="fbox">
	<table summary="서버" class="tbl01">
	<colgroup><col width="33%" /><col width="" /></colgroup>
	<tbody>
		<tr>
		<th>dynamic.classpath</th>
		<td style="text-align:left"><%=irConfig.getString("dynamic.classpath") %></td>
		</tr>
	</tbody>
	</table>
	</div>
	<!-- 
	<div id="btnBox">
	<a href="javascript:document.configForm.submit()" class="btn">저장</a>
	</div>
 	-->
	<!-- E : #mainContent --></div>

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	

</body>

</html>

