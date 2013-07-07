<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="java.io.File"%>
<%@page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.config.Schema"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.ir.config.DataSourceConfig"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.ir.config.FieldSetting"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="org.fastcatsearch.service.*"%>

<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLDecoder"%>

<%@include file="../common.jsp" %>

<%
	String message = URLDecoder.decode(WebUtils.getString(request.getParameter("message"), ""),"utf-8");
	IRService irService = ServiceManager.getInstance().getService(IRService.class);
	IRConfig irConfig = IRSettings.getConfig(true);
	String collectinListStr = irConfig.getString("collection.list");
	String[] colletionList = collectinListStr.split(",");
	
	String collection = request.getParameter("collection");
	String sourceType = request.getParameter("sourceType");
	
	if(collection == null){
		if(colletionList.length > 0)
	collection = colletionList[0];
		
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
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.validate.min.js" type="text/javascript"></script>
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/validate.messages_ko.js" type="text/javascript"></script>
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/help.js" type="text/javascript"></script>
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-ui-1.8.9.min.js" type="text/javascript"></script>
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.fileupload.js" type="text/javascript"></script>
	<script src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.fileupload-ui.js" type="text/javascript"></script>
	<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.easyui.min.js"></script>
	<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.edatagrid.js"></script> 
	<script type="text/javascript">
	
	$(document).ready(function() {
		var collection = '<%=collection%>';
		$("#saveForm").validate({
			errorClass : "invalidValue",
			submitHandler: function(form) {
				form.submit();
				return true;
			},
			rules: {
				fetchsize : {number: true, max: 10000, min: 100},
				bulksize : {number: true, max: 1000, min: 50}
			}
		});
	});

	
	function alertMessage(){
		var message = "<%=message%>";
		if(message != "")
			alert(message);
	}
	function selectCollection(dropdown){
		var myindex = dropdown.selectedIndex
	    var selValue = dropdown.options[myindex].value
		location.href="?collection="+selValue;
		return true;
	}

	function selectSourceType(mySource){
		location.href="?collection=<%=collection%>&sourceType="+mySource.value;
		return true;
	}
	
	function checkForm(form){
		if(form.sourceType[0].checked){
			//DB
			if(form.fullFilePath.value == "" || form.fileDocParser.value == "" || form.fileEncoding.value == ""){
				alert("필수항목을 채워주세요.");
				return false;
			}
			return true;
		}else if(form.sourceType[1].checked){
			//FILE
			if(form.driver.value == "" || form.url.value == "" || form.user.value == "" || form.password.value == ""
				|| form.fetchsize.value == "" || form.fullQuery.value == "" ){
				alert("필수항목을 채워주세요.");
				return false;
			}
			return true;
		}else if(form.sourceType[2].checked){
			//CUSTOM
			if(form.crawlFullConfig == ""){
				alert("필수항목을 채워주세요.");
				return false;
			}
			return true;
		}
		alert("소스타입을 선택해주세요.");
		return false;
	}

	</script>
	<script type="text/javascript"> 

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
		         	alert("성공적으로 추가되었습니다.");
	         	}else{
		         	alert("추가 실패입니다. 파일의 형식에 오류가 있는지 확인해보세요.");
	         	}
	         } else {
	             // Instead of an XHR object, an iframe is used for legacy browsers:
	              var resp = jQuery.trim(xhr.contents().text());
	         	if(resp == 1){
	         		alert("성공적으로 추가되었습니다.");
	         	}else{
	         		alert("추가 실패입니다. 파일의 형식에 오류가 있는지 확인해보세요.");
	         	}
	         }
	         location.reload();
	     }
	 });
	});
</script> 
</head>

<body onload="alertMessage()">
<div id="container">
<!-- header -->
<%@include file="../header.jsp" %>

<div id="sidebar">
	<div class="sidebox">
		<h3>컬렉션</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>collection/main.jsp">컬렉션정보</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>collection/schema.jsp">스키마설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>collection/datasource.jsp" class="selected">데이터소스설정</a></li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

<div id="mainContent">
	<h2>데이터소스 설정</h2>
<%
	if(!"".equals(collection)) {


		DataSourceConfig dataSourceSetting = IRSettings.getDatasource(collection, true);
		if(sourceType == null)
	sourceType = dataSourceSetting.sourceType;
		
		String sourceFrom = dataSourceSetting.sourceFrom;
		if(sourceFrom == null)
	sourceFrom = "SINGLE";
		
		if(sourceType == null || sourceType.length() == 0)
	sourceType = "DB";
%>

<form action="collectionService.jsp" method="post" name="saveForm" id="saveForm" onsubmit="return checkForm(this)">
<input type="hidden" name="cmd" value="6" />
<input type="hidden" name="collection" value="<%=collection%>" />

	<div class="fbox">
	<table summary="데이터소스 설정" class="tbl01">
	<colgroup><col width="23%" /><col width="" /></colgroup>
	<tbody>
	<tr>
		<th>컬렉션명</th>
		<td class="r">
			<select name="chooseCollection" onchange="javascript:selectCollection(this)">
			<%
			//setting default collection name
			for(int i = 0;i < colletionList.length;i++){
				String col = colletionList[i];
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
		<th>데이터소스종류</th>
		<td class="r">
			<input type="radio" name="sourceType" value="FILE" <%=sourceType.equals("FILE") ? "checked" : "" %> onclick="selectSourceType(this)" />파일
			<input type="radio" name="sourceType" value="DB" <%=sourceType.equals("DB") ? "checked" : "" %> onclick="selectSourceType(this)" />데이터베이스
			<input type="radio" name="sourceType" value="WEB" <%=sourceType.equals("WEB") ? "checked" : "" %> onclick="selectSourceType(this)" />웹페이지
			<input type="radio" name="sourceType" value="CUSTOM" <%=sourceType.equals("CUSTOM") ? "checked" : "" %> onclick="selectSourceType(this)" />사용자설정
			<input type="hidden"  id="sourceTypeList" class="help" />
		</td>
	</tr>
	<tr>
		<th>데이터소스 수집방식</th>
		<td class="r">
			<input type="radio" name="sourceFrom" value="SINGLE" <%=sourceFrom.equals("SINGLE") ? "checked" : "" %> />싱글소스
			<input type="radio" name="sourceFrom" value="MULTI" <%=sourceFrom.equals("MULTI") ? "checked" : "" %> />멀티소스
			<input type="hidden"  id="sourceFromList" class="help" />
		</td>
	</tr>
	<tr class="last">
		<th>소스모디파이어</th>
		<td class="r"><%=setTextView(dataSourceSetting.sourceModifier, "sourceModifier", true, true) %></td>
	</tr>
	</tbody>
	</table>
	</div>

<%
	boolean enableDB = false;
	boolean enableFILE = false;
	boolean enableCustom = false;
	
	if(sourceType.equals("DB")){
		enableDB = true;
	}
	if(sourceType.equals("FILE")){
		enableFILE = true;
	}
	if(sourceType.equals("CUSTOM")){
		enableCustom = true;
	}
%>

	<h2>FILE</h2>
	<div class="fbox">
	<table summary="데이터소스 설정" class="tbl01">
	<colgroup><col width="23%" /><col width="" /></colgroup>
	<tbody>
	<tr>
		<th>전체색인 파일경로</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.fullFilePath, "fullFilePath", enableFILE, true) %></td>
	</tr>
	<tr>
		<th>증분색인 파일경로</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.incFilePath, "incFilePath", enableFILE, true) %></td>
	</tr>
	<tr>
		<th>수집파일Parser</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.fileDocParser, "fileDocParser", enableFILE, true) %></td>
	</tr>
	<tr>
		<th>수집파일 인코딩</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.fileEncoding, "fileEncoding", enableFILE, 20, true) %></td>
	</tr>
	</tbody>
	</table>
	</div>
	<div id="btnBox">
	<a href="javascript:document.saveForm.submit()" class="btn">저장</a>
	</div>

	<h2>DB</h2>
	<div class="fbox">
	<table summary="데이터소스 설정" class="tbl01">
	<colgroup><col width="23%" /><col width="" /></colgroup>
	<tbody>
	<tr>
		<th>JDBC 드라이버</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.driver, "driver", enableDB, true) %></td>
	</tr>
	<tr>
		<th>JDBC URL</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.url, "url", enableDB, true) %></td>
	</tr>
	<tr>
		<th>사용자이름</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.user, "user", enableDB) %></td>
	</tr>
	<tr>
		<th>비밀번호</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.password, "password", enableDB) %></td>
	</tr>
	<tr>
		<th>Fetch Size</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.fetchSize+"", "fetchsize", enableDB, 20, true) %>
		</td>
	</tr>
	<tr>
		<th>Bulk Size</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.bulkSize+"", "bulksize", enableDB, 20, true) %></td>
	</tr>
	
	<tr>
		<th>전체색인 BEFORE 쿼리</th>
		<td style="text-align:left;"><%=setTextAreaView(dataSourceSetting.beforeFullQuery, "beforeFullQuery", enableDB, 4, true) %></td>
	</tr>
	<tr>
		<th>증분색인 BEFORE 쿼리</th>
		<td style="text-align:left;"><%=setTextAreaView(dataSourceSetting.beforeIncQuery, "beforeIncQuery", enableDB, 4, true) %></td>
	</tr>
	<tr>
		<th>전체색인쿼리 *</th>
		<td style="text-align:left;"><%=setTextAreaView(dataSourceSetting.fullQuery, "fullQuery", enableDB, 10, true) %></td>
	</tr>
	<tr>
		<th>삭제문서아이디 쿼리</th>
		<td style="text-align:left;"><%=setTextAreaView(dataSourceSetting.deleteIdQuery, "deleteIdQuery", enableDB, 4, true) %></td>
	</tr>
	<tr>
		<th>증분색인쿼리 *</th>
		<td style="text-align:left;"><%=setTextAreaView(dataSourceSetting.incQuery, "incQuery", enableDB, 10, true) %></td>
	</tr>
	<tr>
		<th>전체색인 AFTER 쿼리</th>
		<td style="text-align:left;"><%=setTextAreaView(dataSourceSetting.afterFullQuery, "afterFullQuery", enableDB, 10, true) %></td>
	</tr>
	<tr>
		<th>증분색인 AFTER 쿼리</th>
		<td style="text-align:left;"><%=setTextAreaView(dataSourceSetting.afterIncQuery, "afterIncQuery", enableDB, 10, true) %></td>
	</tr>
	
	<tr>
		<th>전체색인 백업파일경로</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.fullBackupPath, "fullBackupPath", enableDB, true) %></td>
	</tr>
	<tr>
		<th>증분색인 백업파일경로</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.incBackupPath, "incBackupPath", enableDB, true) %></td>
	</tr>
	<tr>
		<th>백업파일 인코딩</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.backupFileEncoding, "backupFileEncoding", enableDB, true) %></td>
	</tr>
	</tbody>
	</table>
	</div>
	<div id="btnBox">
	<a href="javascript:document.saveForm.submit()" class="btn">저장</a>
	</div>
	
	<h2>사용자설정수집</h2>
	<div class="fbox">
	<table summary="사용자설정" class="tbl01">
	<colgroup><col width="23%" /><col width="" /></colgroup>
	<tbody>
	<tr>
		<th>데이터소스 Reader</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.customReaderClass, "customReaderClass", enableCustom, true) %></td>
	</tr>
	<tr>
		<th>사용자설정파일</th>
		<td style="text-align:left;"><%=setTextView(dataSourceSetting.customConfigFile, "customConfigFile", enableCustom, true) %></td>
	</tr>
	</tbody>
	</table>
	</div>
	<div id="btnBox">
	<a href="javascript:document.saveForm.submit()" class="btn">저장</a>
	</div>
	
</form>

	<h2>웹페이지</h2>
	<div class="fbox">
	<%if(sourceType.equals("WEB")){ %>
	<iframe src="webtabs.jsp?collection=<%=collection%>" frameborder="no" border="0" marginwidth="0" marginheight="0" scrolling="no" allowtransparency="yes" style="width:100%;height:400px;" ></iframe>
	<%} %>
	<form id="file_upload" name="file_upload" action="../dic/fileInsertService.jsp" method="post" enctype="multipart/form-data">
		<input type="hidden" name="cmd" value="3"/>
		<input type="hidden" name="collection" value="<%=collection%>"/>
		<input type="file" name="file"/>
		<div style="font-size:8pt">파일로추가</div>
	</form>
	<table id="files" align="right" style="margin-right:10px"></table>
	</div>
	<!-- E : #mainContent --></div>
	
<!-- footer -->
<%
	} else {
%>
	<div class="fbox">
	<div style="font-size:8pt;padding:10px 0 10px 0;" align="center">컬렉션이 존재하지 않습니다.</div>
	</div>
<%
	}
%>
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
	
</body>

</html>

<%!public String setTextView(String value, String name, boolean isEnabled){
		return setTextView(value, name, isEnabled, 85, false);
	}
	public String setTextView(String value, String name, boolean isEnabled, boolean hasHelp){
		return setTextView(value, name, isEnabled, 85, hasHelp);
	}
	public String setTextView(String value, String name, boolean isEnabled, int size){
		return setTextView(value, name, isEnabled, size, false);
	}
	public String setTextView(String value, String name, boolean isEnabled, int size, boolean hasHelp){

		if(isEnabled){
			if(value == null)
				value = "";
			
			return "<input type='text' name='"+name+"' id='"+name+"' value='"+value+"' size='"+size+"' class='inp02"+(hasHelp?" help":"")+"' >";
		}else{
			if(value == null || value.length() == 0)
				value = "&nbsp;";
				
			return value;
		}
	}
	public String setTextAreaView(String value, String name, boolean isEnabled, int rows){
		return setTextAreaView(value, name, isEnabled, rows);
	}
	public String setTextAreaView(String value, String name, boolean isEnabled, int rows, boolean hasHelp){
		if(isEnabled){
			if(value == null)
				value = "";
			
			return "<textarea type='text' class='"+(hasHelp?" help":"")+"' name='"+name+"' id='"+name+"' cols='85' rows='"+rows+"'>"+value+"</textarea>";
		}else{
			if(value == null || value.length() == 0)
				value = "&nbsp;";
				
			return value;
		}
	}%>
