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
		
		$.ajax({
		  url: 'collectionService.jsp?cmd=16&collection='+collection,
		  success: function(data) {
		   data = eval(data);
		    $.each(data, function(i, row){
		    	var tabHtml = '<iframe src="webpagesource.jsp?collection='+collection+'&cate='+row.cate+'" frameborder="no" border="0" marginwidth="0" marginheight="0" scrolling="no" allowtransparency="yes" style="width:100%;height:400px;" ></iframe>';
				addTab(row.cate,tabHtml);
			});
		  }
		});
	});
	

	</script>
	<script type="text/javascript"> 

	function addTab(cate,content){
			$('#divTab').tabs('add',{
				title:cate,
				content:content,
				iconCls:'icon-save',
				closable:true
			});
		}
	
</script> 
</head>

<body>
	
	<div id="divTab" class="easyui-tabs" style="width:auto;height:auto">

	</div>
</body>

</html>
