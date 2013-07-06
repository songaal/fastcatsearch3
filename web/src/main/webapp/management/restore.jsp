<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.search.SegmentInfo"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="java.net.URLDecoder"%>

<%@include file="../common.jsp" %>

<%

	String message = URLDecoder.decode(WebUtils.getString(request.getParameter("message"), ""),"utf-8");

	IRConfig irConfig = IRSettings.getConfig(true);
	String collectinListStr = irConfig.getString("collection.list");
	String[] colletionList = collectinListStr.split(",");
	
	String collection = WebUtils.getString(request.getParameter("collection"),"");
	int segment = WebUtils.getInt(request.getParameter("segment"),-1);
	
	IRService irService = ServiceManager.getInstance().getService(IRService.class);
	int[] segmentList = null;
	CollectionHandler collectionHandler = null;
	
	if(collection.length() > 0){
		collectionHandler = irService.getCollectionHandler(collection);
		System.out.println(collection+" : "+collectionHandler);
		segmentList = collectionHandler.getAllSegmentNumberList();
	}else{
		segmentList = new int[0];
	}
	
	int lastRevision = -1;
	SegmentInfo segmentInfo = null;
	if(segment >= 0){
		segmentInfo = collectionHandler.getSegmentInfo(segment);
		lastRevision = segmentInfo.getLastRevision();
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
	<script type="text/javascript">
		var collection = "<%=collection %>";
		var segment = "<%=segment %>";

		function alertMessage(){
			var message = "<%=message %>";
			if(message != "")
				alert(message);
		}
		
		function selectCollection(dropdown){
			var myindex  = dropdown.selectedIndex
		    var selValue = dropdown.options[myindex].value
			location.href="?collection="+selValue;
			return true;
		}
		function selectSegment(dropdown){
			var myindex  = dropdown.selectedIndex
		    var selValue = dropdown.options[myindex].value
			location.href="?segment="+selValue+"&collection="+collection;
			return true;
		}
		function restoreCollection(){
			form = document.restoreForm;
			if(form.collection.value == "" || form.segment.value == "" || form.revision.value == ""){
				alert("값을 선택해주세요.");
				return;
			}
			document.restoreForm.submit()
			return true;
		}
	</script>
</head>

<body onload="alertMessage()">

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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/advConfig.jsp">고급설정보기</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/restore.jsp" class="selected">복원</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/license.jsp">라이선스</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>이전 색인시점으로 색인데이터를 복원한다.</li>
			<li>현재시점보다 이전의 색인데이터를 선택해야 한다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">

<form action="managementService.jsp" method="post" name="restoreForm">
<input type="hidden" name="cmd" value="2" />

	<h2>복원</h2>
	<div class="fbox">
	<table summary="컬렉션" class="tbl01">
	<colgroup><col width="20%" /><col width="" /></colgroup>
	<tbody>
	<tr>
		<th>컬렉션</th>
		<td style="text-align:left">
		<select name="collection" onchange="javascript:selectCollection(this)">
		<option value="">-</option>
		<%
		for(int i = 0; i < colletionList.length; i++){
			String col = colletionList[i];
			if(irService.getCollectionHandler(col) == null){
				continue;
			}
			if(collection == null){
				if(i == 0){
					collection = col;
				}
			}
			%>
			<option value="<%=col %>" <%=col.equals(collection) ? "selected" : "" %> ><%=col %></option>
			<%
		}
		%>
		</select>
		</td>
	</tr>
	<tr>
		<th>세그먼트</th>
		<td style="text-align:left">
		<select name="segment" onchange="javascript:selectSegment(this)">
		<option value="">-</option>
		<%
		
		for(int i = 0; i < segmentList.length; i++){
			int seg = segmentList[i];
			if(seg < 0){
				if(i == 0){
					segment = seg;
				}
			}
			%>
			<option value="<%=seg %>" <%=segment == seg ? "selected" : "" %> ><%=seg %></option>
			<%
		}
		%>
		</select>
		<%
			if(segmentInfo != null){
				out.println("문서 "+segmentInfo.getDocCount()+"건");
			}
		%>
		</td>
	</tr>
	<tr>
		<th>리비전</th>
		<td style="text-align:left">
		<select name="revision">
		<option value="">-</option>
		<%
		for(int i = 0; i <= lastRevision; i++){
			%>
			<option value="<%=i %>" <%=(i == lastRevision) ? "selected" : ""%>><%=i %></option>
			<%
		}
		%>
		</select>
		</td>
	</tr>
	</tbody>
	</table>
	</div>
	
	<div id="btnBox">
	<a href="javascript:restoreCollection()" class="btn">복원</a>
	</div>
	
</form>
	<!-- E : #mainContent --></div>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	
</body>

</html>
