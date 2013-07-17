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
<%@page import="org.fastcatsearch.ir.config.FieldSetting"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="org.fastcatsearch.ir.common.SettingException"%>
<%@page import="org.fastcatsearch.ir.analysis.*"%>
<%@page import="org.fastcatsearch.service.*"%>

<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="java.util.*"%>
<%@page import="java.net.URLDecoder"%>

<%@include file="../common.jsp" %>

<%
	String message = URLDecoder.decode(WebUtils.getString(request.getParameter("message"), ""),"utf-8");
	IRConfig irConfig = IRSettings.getConfig(true);
	IRService irService = ServiceManager.getInstance().getService(IRService.class);
 	String[] typeList = {"int","long","float","double","datetime","achar","uchar"};
 	//String[][] indexList = irService.getTokenizers();
	String collectinListStr = irConfig.getString("collection.list");
	String[] colletionList = collectinListStr.split(",");
	String collection = request.getParameter("collection");
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
<script src="<%=FASTCAT_MANAGE_ROOT%>js/schema.js" type="text/javascript"></script> 
<script type="text/javascript">
	editableTable();
	//ischange();
	function alertMessage(){
		var message = "<%=message%>";
		if(message != "")
			alert(message);
	}
	
	function selectCollection(dropdown){
		var myindex  = dropdown.selectedIndex
	    var selValue = dropdown.options[myindex].value
		location.href="?collection="+selValue;
		return true;
	}

	</script>
</head>

<body>
<div id="container">
<!-- header -->
<%@include file="../header.jsp" %>

<div id="sidebar">
	<div class="sidebox">
		<h3>컬렉션</h3>
		<ul class="latest">
		<li><a href="<%=FASTCAT_MANAGE_ROOT%>collection/main.jsp">컬렉션정보</a></li>
		<li><a href="<%=FASTCAT_MANAGE_ROOT%>collection/schema.jsp">스키마설정</a></li>
		<li><a href="<%=FASTCAT_MANAGE_ROOT%>collection/datasource.jsp">데이터소스설정</a></li>
		</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
		<ul class="latest">
		<li>주키 : 문서를 구별하는 키. 한 컬렉션에 반드시 하나를 선택해야 한다.</li>
		<li>타입 : 필드유형. Int, long, float, double, datetime, achar, uchar 중 선택가능.</li>
		<li>사이즈 : 필드가 achar, uchar일 경우 길이를 설정. -1일 경우 가변길이로 동작함.</li>
		<li>색인 : 검색대상필드여부. 한 컬렉션에 반드시 하나이상을 셋팅해야 한다.</li>
		<li>정렬 : 정렬필드여부</li>
		<li>그룹 : 통계대상필드여부</li>
		<li>필터 : 필터링대상필드여부</li>
		<li>저장여부 : 보여줄 필드가 아닐경우 저장하지 않는 기능.</li>
		<li>정규화 : 특수글자코드를 일반글자코드로 변경</li>
		<li>컬럼 : 그룹핑 함수의 sum, max 에서 쓰기 위해 체크 되어야 함, 그외 여러가지 용도로 활용가능</li>
		<li>가상필드 : 확장기능을 통해 데이터를 입력받는 필드</li>
		<li>Modify : 확장기능을 필드데이터를 변경여부</li>
		</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">

	<h2>스키마 작업본 편집</h2>
	컬렉션명 : <b><span id="chooseCollection"><%=collection %></span></b>
	<br></br>	
	<input type="button" value="완료" class="btn_del" onclick="finish('<%=collection%>')"/>
	<input type="button" value="필드추가 +" class="btn_del" onclick="addField('<%=collection%>')"/>
	<input type="button" value="필드삭제 -" class="btn_del" onclick="deleteField('<%=collection%>')"/>
	<input type="button" value="작업본삭제" class="btn_del" onclick="deleteWorkSchema('<%=collection%>')"/>
	<br/><br/>
	 
	<% String errorMsg = null; %>
	<% Schema workingSchema = null; %>
	<% try{ %>
		<% workingSchema = IRSettings.getWorkSchema(collection, true, true); %>
	<% }catch(SettingException e){ %>
		<% errorMsg = e.getMessage(); %>
	<% } %>
	<% if(errorMsg != null){ %>
		<h3 style="color:#663366">작업본</h3>
		<div class="fbox" id="hiddenDiv">
		<table summary="스키마 설정" class="tbl02">
		<tbody>
		<tr>
		<td class="first">
		<p> 셋팅에 에러가 존재합니다. 작업본을 삭제해주십시오.
		<br/><br/>
		에러내용 : <%=errorMsg %>		
		</p>
		</td></tr>
		</tbody>
		</table>
		</div>
	<% } %>
	<% if(workingSchema != null) { %>
		<div class="fbox" id="shownDiv" > 
		<table summary="스키마 설정" class="tbl02">
		<colgroup><col width="3%" /><col width="" /><col width="5%" /><col width="7%" /><col width="7%" /><col width="" /><col width="5%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /></colgroup>
		<thead>
		<tr>
			<!-- <th>&nbsp;</th> -->
			<th class="first">선택</th>
			<th>필드명</th>
			<th>주키</th>
			<th>타입</th>
			<th>사이즈</th>
			<th>색인</th>
			<th>정렬</th>
			<th>정렬사이즈</th>
			<th>그룹</th>
			<th>필터</th>
			<th>저장여부</th>
			<th>정규화</th>
			<th>컬럼</th>
			<th>가상필드</th>
			<th>Modify</th>
			<th>태그제거</th>
			<th>다중값</th>
		</tr>
		</thead>
		<tbody>
		<% //Schema workingSchema = IRSettings.getWorkSchema(collection, true, true); %>
		<% List<FieldSetting> wfieldSettingList = workingSchema.getFieldSettingList();
		
		%>
		<% for(int i = 0; i < wfieldSettingList.size(); i++) { %>
			<% FieldSetting fieldSetting = wfieldSettingList.get(i); %>
			<% String index = ""; %>
			<% if(fieldSetting.indexSetting != null) { %>
				<% index = fieldSetting.indexSetting.indexAnalyzerName; %>
			<% }else{ %>
				<% index = ""; %>
			<% } %>
		<tr>
			<td class="first"><input type="radio" name="selectField" value="<%=fieldSetting.name%>" /></td>
			<td class="editFieldName" id="01<%=fieldSetting.name%>"><strong class="small tb"><%=fieldSetting.name%></strong></td>
			<td >
				<% if(fieldSetting.primary) { %>
				<input type='checkbox'  id='02<%=fieldSetting.name%>' class='chk' checked />
				<% }else {%>
				<input id='02<%=fieldSetting.name%>' type='checkbox' class='chk' />
				<% } %>
			</td>
			<td >
				<select id="03<%=fieldSetting.name%>" class="slt">
				<% String fieldType = fieldSetting.type.toString().toLowerCase(); %>
				<% for(int j = 0;j < typeList.length;j++){ %>
					<option value="<%=typeList[j]%>" <% if(typeList[j].equals(fieldType)){ %> selected <%} else {%> <%}%> ><%=typeList[j]%></option>
				<% } %>
				</select>
			</td>
			<td class="editable" id="04<%=fieldSetting.name%>" title="사이즈는 최대 256까지 설정가능합니다.">
				<% if(fieldType.equals("int") || fieldType.equals("float")){ %>
					<% out.print("4"); %>
				<% } else if(fieldType.equals("long") || fieldType.equals("double") || fieldType.equals("datetime")) { %>
					<% out.print("8"); %>
				<% }else{ %>
					<% out.print(fieldSetting.size); %>
				<% } %>
			</td>
			<td>
				<select id="05<%=fieldSetting.name%>" class="sltindex">
					<option value=""></option>
				<%
				Map<String, AnalyzerPool> poolMap = IRSettings.getAnalyzerPoolManager().getPoolMap(collection);
				if(poolMap != null){
					Iterator<String> iter = poolMap.keySet().iterator();
				
					while(iter.hasNext()){ 
						String analyzerId = iter.next();
					%>
					<option value="<%=analyzerId%>" <% if(analyzerId.equals(index)){ %> selected <%}else {%> <%}%> ><%=analyzerId%></option>
					<% } %>
				<%
				}
				%>
				</select>
			</td>
			<td ><% if(fieldSetting.sortSetting != null) { %><input type='checkbox'  id='06<%=fieldSetting.name%>' class='chk' checked /><% }else {%><input id='06<%=fieldSetting.name%>' type='checkbox' class='chk' /><%} %></td>
			<td class="editable" id="07<%=fieldSetting.name%>"><%=(fieldSetting.sortSetting != null && fieldSetting.sortSetting.sortSize > 0) ? fieldSetting.sortSetting.sortSize+"" : "&nbsp;" %></td>
			<td ><% if(fieldSetting.groupSetting != null) { %><input type='checkbox'  id='08<%=fieldSetting.name%>' class='chk' checked /><% }else {%><input id='08<%=fieldSetting.name%>' type='checkbox' class='chk' /><%} %></td>
			<td ><% if(fieldSetting.filterSetting != null) { %><input type='checkbox'  id='09<%=fieldSetting.name%>' class='chk' checked /><% }else {%><input id='09<%=fieldSetting.name%>' type='checkbox' class='chk' /><%} %></td>
			<td ><% if(fieldSetting.store) { %><input type='checkbox'  id='10<%=fieldSetting.name%>' class='chk' checked /><% }else {%><input id='10<%=fieldSetting.name%>' type='checkbox' class='chk' /><%} %></td>
			<td ><% if(fieldSetting.normalize) { %><input type='checkbox'  id='11<%=fieldSetting.name%>' class='chk' checked/><% }else {%><input id='11<%=fieldSetting.name%>' type='checkbox' class='chk' /><%} %></td>
			<td ><% if(fieldSetting.column) { %><input type='checkbox'  id='12<%=fieldSetting.name%>' class='chk' checked/><% }else {%><input id='12<%=fieldSetting.name%>' type='checkbox' class='chk' /><%} %></td>			
			<td ><% if(fieldSetting.virtual) { %><input type='checkbox'  id='13<%=fieldSetting.name%>' class='chk' checked/><% }else {%><input id='13<%=fieldSetting.name%>' type='checkbox' class='chk' /><%} %></td>
			<td><% if(fieldSetting.modify) { %><input type='checkbox'  id='14<%=fieldSetting.name%>' class='chk' checked/><% }else {%><input id='14<%=fieldSetting.name%>' type='checkbox' class='chk' /><%} %></td>
			<td><% if(fieldSetting.tagRemove) { %><input type='checkbox'  id='15<%=fieldSetting.name%>' class='chk' checked/><% }else {%><input id='15<%=fieldSetting.name%>' type='checkbox' class='chk' /><%} %></td>
			<td><% if(fieldSetting.multiValue) { %><input type='checkbox'  id='16<%=fieldSetting.name%>' class='chk' checked/><% }else {%><input id='16<%=fieldSetting.name%>' type='checkbox' class='chk' /><%} %></td>
		</tr>
		<% } %>	
		</tbody>
		</table>
		</div>
	<% } //if(workingSchema != null){ %>
	<!-- E : #mainContent --></div>
	<!-- footer -->
<%@include file="../footer.jsp" %>
</div><!-- //E : #container -->
<!-- -->
<form name="addForm" id="addForm">
	<input type="hidden" name="collection" />
	<input type="hidden" name="field" />
	<input type="hidden" name="pk" />
	<input type="hidden" name="type" />
	<input type="hidden" name="size" />
	<input type="hidden" name="index" />
	<input type="hidden" name="sort" />
	<input type="hidden" name="sortSize" />
	<input type="hidden" name="group" />
	<input type="hidden" name="filter" />
	<input type="hidden" name="store" />
	<input type="hidden" name="normalize" />
	<input type="hidden" name="virtual" />
	<input type="hidden" name="modifier" />
	<input type="hidden" name="column" />
	<!--input type="hidden" name="tagRemove" />
	<input type="hidden" name="multiValue" /-->
</form>
</body>
</html>
