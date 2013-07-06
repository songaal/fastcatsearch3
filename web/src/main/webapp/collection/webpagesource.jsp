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
<%@page import="org.fastcatsearch.ir.config.FieldSetting"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>

<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="java.net.URLDecoder"%>

<%@include file="../common.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>FASTCAT 검색엔진 관리도구</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/jquery.fileupload-ui.css" rel="stylesheet" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" type="text/css" href="<%=FASTCAT_MANAGE_ROOT%>css/easyui.css">
<link rel="stylesheet" type="text/css" href="<%=FASTCAT_MANAGE_ROOT%>css/icon.css">
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
		var collection = '<%=request.getParameter("collection")%>';
		var cate = '<%=request.getParameter("cate")%>';
		$('#dg').edatagrid({  
			height: 'auto',
			pagination:true,
		    url: 'collectionService.jsp?cmd=12&collection='+collection+'&cate='+cate,  
		    saveUrl: 'collectionService.jsp?cmd=13&collection='+collection,  
		    updateUrl: 'collectionService.jsp?cmd=14&collection='+collection,  
		    destroyUrl: 'collectionService.jsp?cmd=15&collection='+collection  
		});  
	});


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
	
	function addTab(cate,content){

			$('#divTab').tabs('add',{

				title:cate,

				content:content,

				iconCls:'icon-save',

				closable:true,

				tools:[{

					iconCls:'icon-mini-refresh',

					handler:function(){

						alert('refresh');

					}

				}]

			});

		}
	
</script> 
</head>

<body>
	
	<table id="dg" title="url.list" style="width:1510px;height:250px"  
        rownumbers="true" fitColumns="true" singleSelect="true">  
	    <thead>  
	        <tr>  
	            <th field="id" width="50" >id</th>  
	            <th field="link" width="50" editor="{type:'validatebox',options:{required:true}}">link</th>  
	            <th field="title" width="50" editor="text">title</th>  
	            <th field="encoding" width="50" editor="text">encoding</th>
	            <th field="cate" width="50" editor="text">cate</th>  
	            <th field="upt_dt" width="50" editor="text">upt_dt</th>  
	        </tr>  
	    </thead>  
	</table>  
			
</body>

</html>
