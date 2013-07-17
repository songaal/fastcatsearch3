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
<%@page import="org.fastcatsearch.service.*"%>

<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@include file="../common.jsp" %>
<%
	String message = WebUtils.getString(request.getParameter("message"), "");

	IRService irService = ServiceManager.getInstance().getService(IRService.class);
	//String[] colNames = irService.getCollectionNames();
 	//String[][] indexList = irService.getTokenizers();
	IRConfig irConfig = IRSettings.getConfig(true);
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

	function addField(){
		
		if(value != ""){
			location.href = "collectionService.jsp?cmd=4&collection=<%=collection%>&field="+field;
		}
	}

	function removeField(){
		var x = document.getElementsByName("selectField");
		field = "";
		for(i=0;i<x.length;i++){
			if(x[i].checked){
				field = x[i].value;
				break;
			}
		}

        if(field == "")
            alert("삭제할 필드를 선택해주세요.");
        else
			location.href = "collectionService.jsp?cmd=5&collection=<%=collection %>&field="+field;
		
	}
	
	function makeSchema(collection){
		if(confirm("작업본을 자동생성하시겠습니까? 수정된 작업본은 다음 전체색인시 적용됩니다.")){

			$.ajax({
				url:"makeSchema.jsp",
				method:"post",
				data:{collection:collection},
				success:function(data) {
					data=data.replace(/^\s\s*/,"");
					alert(data);
					window.location.href="schema.jsp?collection="+collection;
				}
			});
		}
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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>collection/schema.jsp" class="selected">스키마설정</a></li>
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
			<li>컬럼 : 그룹핑 함수의 sum, max 에서 쓰기 위해 체크 되어야 함, 그외 여러가지 용도로 활용 가능</li>
			<li>가상필드 : 확장기능을 통해 데이터를 입력받는 필드</li>
			<li>Modify : 확장기능을 필드데이터를 변경여부</li>			
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
	<h2>스키마 설정</h2>
	컬렉션명 :
	<select id="chooseCollection" onchange="javascript:selectCollection(this)">
	<% for(int i = 0;i < colletionList.length;i++){ %>
		<% String col = colletionList[i]; %>
		<% if(collection == null){ %>
			<% if(i == 0){ %>
				<% collection = col; %>
			<% } %>
		<% } %>
		<option value="<%=col %>" <%=col.equals(collection) ? "selected" : "" %> ><%=col %></option>
	<% } %>
	</select>
	<input type="button" value="작업본편집" class="btn_del" onclick="goEditorPage('<%=collection%>');"/>
	<input type="button" value="자동생성" class="btn_del" onclick="javascript:makeSchema('<%=collection%>');"/>
	<br/><br/>
	
	<% String errorMsg = null; %>
	<% Schema workingSchema = null; %>
	<% try{ %>
		<% workingSchema = IRSettings.getWorkSchema(collection, true, false); %>
	<% }catch(SettingException e){ %>
		<% errorMsg = e.getMessage(); %>
	<% } %>
	
	<% if(errorMsg != null){ %>
		<h3 style="color:#663366">작업본</h3>
		<div class="fbox" id="hiddenDiv">
		<table summary="스키마 설정" class="tbl02">
		<tbody>
		<tr><td class="first">스키마에러 : <%=errorMsg %></td></tr>
		</tbody>
		</table>
		</div>
		
	<% } %>
	<% if(workingSchema != null){ %>
		<h3 style="color:#663366">작업본</h3>
		<div class="fbox" id="hiddenDiv">
		<table summary="스키마 설정" class="tbl02">
		<colgroup><col width="4%" /><col width="" /><col width="5%" /><col width="7%" /><col width="7%" /><col width="" /><col width="5%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /></colgroup>
		<thead>
		<tr>
			<th class="first">번호</th>
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
		<% List<FieldSetting> wwfieldSettingList = workingSchema.getFieldSettingList(); %>
		<% for(int i = 0; i < wwfieldSettingList.size(); i++){ %>
			<% FieldSetting fieldSetting = wwfieldSettingList.get(i); %>
			<% String index = ""; %>
			<% if(fieldSetting.indexSetting != null) { %>
				<% index = fieldSetting.indexSetting.indexAnalyzerName; %>
				<%-- <% String indexCls = fieldSetting.indexSetting.indexAnalyzerPool; %>
				<% for(int idxInx=0;idxInx < indexList.length;idxInx++) { %>
					<% if(indexList[idxInx][1].equals(indexCls)) { %>
						<% index = indexList[idxInx][0]; %>
					<% break; } %>
				<% } %> --%>
			<% }else{ %>
				<% index = "&nbsp;"; %>
			<% } %>
		<tr>
			<!-- <td><input type="radio" name="selectField" value="<%=fieldSetting.name%>"></td> -->
			<td class="first"><%=i+1%></td>
			<td id="01<%=fieldSetting.name%>"><strong class="small tb"><%=fieldSetting.name%></strong></td>
			<td id="02<%=fieldSetting.name%>"><%=fieldSetting.primary ? "O" : "&nbsp;" %></td>
			<td id="03<%=fieldSetting.name%>"><%=fieldSetting.type%></td>
			<td id="04<%=fieldSetting.name%>"><%=fieldSetting.size%></td>
			<td id="05<%=fieldSetting.name%>"><%=index %></td>
			<td id="06<%=fieldSetting.name%>"><%=(fieldSetting.sortSetting != null) ? "O" : "&nbsp;" %></td>
			<td id="07<%=fieldSetting.name%>"><%=(fieldSetting.sortSetting != null && fieldSetting.sortSetting.sortSize > 0) ? fieldSetting.sortSetting.sortSize+"" : "&nbsp;" %></td>
			<td id="08<%=fieldSetting.name%>"><%=(fieldSetting.groupSetting != null) ? "O" : "&nbsp;" %></td>
			<td id="09<%=fieldSetting.name%>"><%=(fieldSetting.filterSetting != null) ? "O" : "&nbsp;" %></td>
			<td id="10<%=fieldSetting.name%>"><%=fieldSetting.store ? "O" : "&nbsp;" %></td>
			<td id="11<%=fieldSetting.name%>"><%=fieldSetting.normalize ? "O" : "&nbsp;" %></td>
			<td id="12<%=fieldSetting.name%>"><%=fieldSetting.column ? "O" : "&nbsp;" %></td>
			<td id="13<%=fieldSetting.name%>"><%=fieldSetting.virtual ? "O" : "&nbsp;" %></td>
			<td id="14<%=fieldSetting.name%>"><%=fieldSetting.modify ? "O" : "&nbsp;" %></td>
			<td id="15<%=fieldSetting.name%>"><%=fieldSetting.tagRemove ? "O" : "&nbsp;" %></td>
			<td id="16<%=fieldSetting.name%>"><%=fieldSetting.multiValue ? "O" : "&nbsp;" %></td>
		</tr>
		<% } %>	
		<!--
		<input type="text" value="필드추가" name="sdfsd" class="inp02">
		<input type="button" value="필드삭제" class="btn_s" onclick="javascript:removeField()">
		 -->
		</tbody>
		</table>
		</div>
	<%}%>
	
	
	<h3>원본</h3>
	<div class="fbox">
	<table summary="스키마 설정" class="tbl02">
	<colgroup><col width="4%" /><col width="" /><col width="5%" /><col width="7%" /><col width="7%" /><col width="" /><col width="5%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /><col width="6%" /></colgroup>
	<thead>
	<tr>
		<!-- <th>&nbsp;</th> -->
		<th class="first">번호</th>
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
	<% Schema schema = IRSettings.getSchema(collection, true); %>
	<% List<FieldSetting> fieldSettingList = schema.getFieldSettingList(); %>
	<% for(int i = 0; i < fieldSettingList.size(); i++){ %>
		<% FieldSetting fieldSetting = fieldSettingList.get(i); %>
		<% String index = ""; %>
		<% if(fieldSetting.indexSetting != null){ %>
			<% index = fieldSetting.indexSetting.indexAnalyzerName; %>
			<%-- <% String indexCls = fieldSetting.indexSetting.handler; %>
			<% for(int idxInx=0;idxInx < indexList.length; idxInx++) { %>
			    <% if(indexList[idxInx][1].equals(indexCls)) { %>
			        <% index = indexList[idxInx][0]; %>
		        <% break;} %>
			<% } %> --%>
		<% }else{ %>
			<% index = "&nbsp;"; %>
		<% } %>
		<tr>
			<!-- <td><input type="radio" name="selectField" value="<%=fieldSetting.name%>"></td> -->
			<td class="first"><%=i+1%></td>
			<td><strong class="small tb"><%=fieldSetting.name%></strong></td>
			<td><%=fieldSetting.primary ? "O" : "&nbsp;" %></td>
			<td><%=fieldSetting.type%></td>
			<td><%=fieldSetting.size%></td>
			<td><%=index %></td>
			<td><%=(fieldSetting.sortSetting != null) ? "O" : "&nbsp;" %></td>
			<td><%=(fieldSetting.sortSetting != null && fieldSetting.sortSetting.sortSize > 0) ? fieldSetting.sortSetting.sortSize+"" : "&nbsp;" %></td>
			<td><%=(fieldSetting.groupSetting != null) ? "O" : "&nbsp;" %></td>
			<td><%=(fieldSetting.filterSetting != null) ? "O" : "&nbsp;" %></td>
			<td><%=fieldSetting.store ? "O" : "&nbsp;" %></td>
			<td><%=fieldSetting.normalize ? "O" : "&nbsp;" %></td>
			<td><%=fieldSetting.column ? "O" : "&nbsp;" %></td>
			<td><%=fieldSetting.virtual ? "O" : "&nbsp;" %></td>
			<td><%=fieldSetting.modify ? "O" : "&nbsp;" %></td>
			<td><%=fieldSetting.tagRemove ? "O" : "&nbsp;" %></td>
			<td><%=fieldSetting.multiValue ? "O" : "&nbsp;" %></td>
		</tr>
	<% } %>	
	</tbody>
	</table>
	</div>
	<!-- E : #mainContent --></div>
	<!-- footer -->
<%@include file="../footer.jsp" %>
</div><!-- //E : #container -->
</body>
</html>
