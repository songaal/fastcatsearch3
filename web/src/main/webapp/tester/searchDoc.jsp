<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.analysis.Tokenizer"%>
<%@page import="org.fastcatsearch.ir.io.CharVector"%>

<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.fastcatsearch.ir.config.Schema"%>
<%@page import="org.fastcatsearch.ir.config.DataSourceConfig"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.ir.config.FieldSetting"%>
<%@page import="org.fastcatsearch.util.DynamicClassLoader2"%>

<%@ page import="org.json.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@ page import="java.net.*"%>
<%@ page import="java.io.*"%>


<%@include file="../common.jsp" %>

<%
	String contents = WebUtils.getString(request.getParameter("contents"), "");

	String message = WebUtils.getString(request.getParameter("message"), "");

	IRService irService = ServiceManager.getInstance().getService(IRService.class);
	String[] colletionList = irService.getCollectionNames();
	/* IRConfig irConfig = IRSettings.getConfig(true);
	String collectinListStr = irConfig.getString("collection.list");
	String[] colletionList = collectinListStr.split(","); */

	String cn = WebUtils.getString(request.getParameter("cn"), "");
	
	
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>FASTCAT 검색엔진 관리도구</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/reset.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/jquery-ui.css" type="text/css" rel="stylesheet" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/ui.jqgrid.css" type="text/css" rel="stylesheet" />
<!--[if lte IE 6]>
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style_ie.css" rel="stylesheet" type="text/css" />
<![endif]-->
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/common.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/grid.locale-en.js"></script> 
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.jqGrid.min.js"></script> 
<script type="text/javascript" charset="utf-8">
		var currCollection = '';		
		var utf8_decode = function(utftext) 
		{ 
			var string = ''; 
			var i = 0; 
			var c = c1 = c2 = 0; 
			
			while( i < utftext.length ) {
				c = utftext.charCodeAt(i); 
				
				if( c < 128 ) {
					string += String.fromCharCode(c); 
					i++; 
				} 
				else if( (c > 191) && (c < 224) ) { 
					c2 = utftext.charCodeAt( i+1 ); 
					string += String.fromCharCode( ((c & 31) << 6) | (c2 & 63) ); 
					i += 2; 
				} else{
					c2 = utftext.charCodeAt( i+1 ); 
					c3 = utftext.charCodeAt( i+2 ); 
					string += String.fromCharCode( ((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63) ); 
					i += 3; 
				} 
			} 
			return string; 
		};
		
	function selectCollection(dropdown){
		var myindex  = dropdown.selectedIndex
	    var selValue = dropdown.options[myindex].value
	    currCollection = selValue;
		doingList(selValue,1,5);
	}
	

	$('#search_button').click(function() {
		doingList();
	});
	
	$(window).bind('resize', function() {
		//$("#searchResult").setGridWidth($(window).width()-50);
		$("#searchResult").setGridWidth(740);
	}).trigger('resize');
	
	
	function doingList(collection,start,length){
		if(collection == ""){
			$("#searchResult").html('');
			$("#s_seg").text('');
			$("#s_doc").text('');
			$("#pk").val('');
			return;
		}
		$("#pk").val('');
		$("#searchDiv").html('<input type="text" class="inp02" id="pk" name="pk" value=""/><input type="button" class="btn_c" value="찾기" onclick="doingSearch(currCollection,1,10,encodeURI($(\'#pk\').val()));" />');
		
		collectionName = collection;
		currPageNum = (start+(length-1))/length;
		//최대페이지 번호  
		var maxPage = 10;
		
		if($("#resultWrapper").children())
			$("#resultWrapper").children().remove();
			
		if($("#groupResultWrapper").children())
			$("#groupResultWrapper").children().remove();
		
		$("#SearchResultSummary").show();
		
		$.ajax({
			  url: '<%=FASTCAT_SEARCH_ROOT%>doclist/json',
			  data: {
				admin: true,
				timeout: 5,
				cn: collection,
				sn: start,
				ln: length
			  },
			  type: 'POST',
			  dataType: 'json',
			  error: function(XMLHttpRequest, textStatus, errorThrown) {
			  	  $("#s_now").text(getDatetime());
			  	  $("#s_0").text("JSON ERROR");
			  	  $("#s_1").text(0);
				  $("#s_2").text(0);
				  $("#s_3").text(0);
				  $("#s_4").text(textStatus);
			  },
			  success: function(data_obj) {
			  	$("#s_now").text(getDatetime());
			  	if(data_obj.status > 0){
			  		alert("조회가 실패되었습니다.\nERROR:"+data_obj.error_msg);
			  		$("#chooseCollection").val("none");
			  		return;
			  	}
			  	
			  	
				$("#s_seg").text(data_obj.seg_count);
				$("#s_doc").text(data_obj.doc_count);
				
			  	$("#resultWrapper").append($("<table id='searchResult'></table>"));
				$("#resultWrapper").append($("<div id='pSearchResult'></div>"));

			  	//make fieldname_list
			  	fieldname_list = new Array();

				$.each(data_obj.fieldname_list, function(i, row){
					fieldname_list[i] = row.name
				});
				
				var htmlStr = '<p><div class="fbox"><table summary="조회결과" class="tbl02"><colgroup><col width="10%" />		'+
										 '<col width="14%" /><col width="*" /></colgroup><thead><tr><th class="first">seg-rev-문서번호</th>	'+
										 '<th>필드</th><th>원문</th></tr></thead><tbody> ';	
										
				$.each(data_obj.result, function(i, row_i){
					var trStr = '';
					if(row_i._delete_ == "true"){
						trStr = ';color:#ADADAD';
					}
					
					$.each(fieldname_list, function(j, row_j){
						
						if(j == 0) return true;
						
						var rowNum = 1;
						if(row_i[row_j].length > 60){
							rowNum = 3;	
						}
						
						if(j == 1){
							htmlStr += '<td rowspan="'+(fieldname_list.length-1)+'" class="first">'+(row_i._no_)+'</td><td>'+row_j+'</td><td><textarea style="border-style:none;width:95%'+trStr+'" rows='+rowNum+' readonly="true">'+row_i[row_j]+'</textarea></td></tr>';
						}else{
							htmlStr += '<td>'+row_j+'</td><td><textarea style="border-style:none;width:95%'+trStr+'" rows='+rowNum+' readonly="true">'+row_i[row_j]+'</textarea></td></tr>';
						}					
					});
					
					
				});
				htmlStr += '</tbody></table></div><p>';
				
				/////////////////////////////////////  paging ///////////////////////////////////
				
				htmlStr += '<div class="list_bottom"><div id="paging" class="fl">';
				
	
				
				var totalCount = data_obj.doc_count;
	            
				//그냥 카운트 변수
				var cnt = 0;  
				
				var pageCount = 10;
				        
				var startPage = Math.floor(currPageNum / 10);
				if ((currPageNum % 10) == 0) { 
					startPage = Math.floor((startPage - 1) * 10) +1 ;
				}else{
					startPage = (startPage * 10) +1;
				}                             
				maxPage = Math.floor(totalCount / length);
			
				if (totalCount % length > 0) {
					maxPage = maxPage + 1;
				}
			
				if (currPageNum > 10){
	
					htmlStr += '<span class="num"><a href="javascript:doingList(collectionName,'+((startPage-1)*length-(length-1))+','+length+');">이전</a></span> ';
	
				}else{	
	
					htmlStr += '<span class="num">이전</span> ';
	
				}
				
				var currMaxPageNum = 1;
				
				for (i = startPage;i<=maxPage;i++){
					if (i == currPageNum){
					
						htmlStr += '<span class="num" class="selected">'+i+'&nbsp;</span>'; 
						   
						}else{ 
							  
							  htmlStr += '<span class="num"><a href="javascript:doingList(collectionName,'+(i*length-(length-1))+','+length+');">'+i+'</a></span> ';
						   
						}
						cnt = cnt + 1;
						//카운트가 10개되면 더이상 페이지 번호를 페이지에 찍지않는다.
						if (cnt == 10) { 
							 currMaxPageNum = i;
							 break;		   
						}
	
					}
					
					if ((cnt == 10) && ((i+1) <= maxPage)){  
	
						htmlStr += '<span class="num"><a href="javascript:doingList(collectionName,'+((currMaxPageNum+1)*length-(length-1))+','+length+');" >다음</a></span> ';
					}else{
						htmlStr += '<span class="num">다음</span> ';
					}
	
				
				
				htmlStr += '</div> &nbsp;<input type="text" class="inp04" id="gopage" name="gopage" value=""/> <input type="button" class="btn_c" value="go" onclick="goList(collectionName,'+length+');" /></div>';
			
				
				/////////////////////////////////////  paging ///////////////////////////////////

				
					
				$("#searchResult").html(htmlStr);
				


			  }
		});
	}
	
		function doingSearch(collection,start,length,id){
		if(collection == "")return false;
		collectionName = collection;
		currPageNum = (start+(length-1))/length;
		//최대페이지 번호  
		var maxPage = 10;
		
		if($("#resultWrapper").children())
			$("#resultWrapper").children().remove();
			
		if($("#groupResultWrapper").children())
			$("#groupResultWrapper").children().remove();
		
		$("#SearchResultSummary").show();
		
		$.ajax({
			  url: '<%=FASTCAT_SEARCH_ROOT%>docsearch/json',
			  data: {
				admin: true,
				timeout: 5,
				cn: collection,
				sn: start,
				ln: length,
				id: id
			  },
			  type: 'POST',
			  dataType: 'json',
			  error: function(XMLHttpRequest, textStatus, errorThrown) {
			  	  $("#s_now").text(getDatetime());
			  	  $("#s_0").text("JSON ERROR");
			  	  $("#s_1").text(0);
				  $("#s_2").text(0);
				  $("#s_3").text(0);
				  $("#s_4").text(textStatus);
			  },
			  success: function(data_obj) {
			  	$("#s_now").text(getDatetime());
			  	if(data_obj.status > 0){
					$("#s_seg").text(data_obj.seg_count);
					$("#s_doc").text(data_obj.doc_count);
			  		return;
			  	}
			  	
			  	
				$("#s_seg").text(data_obj.seg_count);
				$("#s_doc").text(data_obj.total_count);
				
			  	$("#resultWrapper").append($("<table id='searchResult'></table>"));
				$("#resultWrapper").append($("<div id='pSearchResult'></div>"));

			  	//make fieldname_list
			  	fieldname_list = new Array();

				$.each(data_obj.fieldname_list, function(i, row){
					fieldname_list[i] = row.name
				});
				
				var htmlStr = '<p><div class="fbox"><table summary="조회결과" class="tbl02"><colgroup><col width="6%" />		'+
										 '<col width="14%" /><col width="80%" /></colgroup><thead><tr><th class="first">문서번호</th>	'+
										 '<th>필드</th><th>원문</th></tr></thead><tbody> ';	
										
				$.each(data_obj.result, function(i, row_i){
					var trStr = '';
					if(row_i._delete_ == "true"){
						trStr = ';color:#ADADAD';
					}else{
						trStr = '';
					}
					
					$.each(fieldname_list, function(j, row_j){
						
						if(j == 0) return true;
						
						var rowNum = 1;
						if(row_i[row_j].length > 60){
							rowNum = 3;	
						}
						
						if(j == 1){
							htmlStr += '<td rowspan="'+(fieldname_list.length-1)+'" class="first">'+(row_i._no_)+'</td><td>'+row_j+'</td><td><textarea style="border-style:none;width:95%'+trStr+'" rows='+rowNum+' readonly="true">'+row_i[row_j]+'</textarea></td></tr>';
						}else{
							htmlStr += '<td>'+row_j+'</td><td><textarea style="border-style:none;width:95%'+trStr+'" rows='+rowNum+' readonly="true">'+row_i[row_j]+'</textarea></td></tr>';
						}					
					});
					
					
				});
				htmlStr += '</tbody></table></div><p>';
				
				/////////////////////////////////////  paging ///////////////////////////////////
				
				htmlStr += '<div class="list_bottom"><div id="paging" class="fl">';
				
	
				
				var totalCount = data_obj.total_count;
	            
				//그냥 카운트 변수
				var cnt = 0;  
				
				var pageCount = 10;
				        
				var startPage = Math.floor(currPageNum / 10);
				if ((currPageNum % 10) == 0) { 
					startPage = Math.floor((startPage - 1) * 10) +1 ;
				}else{
					startPage = (startPage * 10) +1;
				}                             
				maxPage = Math.floor(totalCount / length);
			
				if (totalCount % length > 0) {
					maxPage = maxPage + 1;
				}
			
				if (currPageNum > 10){
	
					htmlStr += '<span class="num"><a href="javascript:doingList(collectionName,'+((startPage-1)*length-(length-1))+','+length+');">이전</a></span>';
	
				}else{	
	
					htmlStr += '<span class="num">이전</span>';
	
				}
				
				var currMaxPageNum = 1;
				for (i = startPage;i<=maxPage;i++){
					if (i == currPageNum){
					
						htmlStr += '<span class="num" class="selected">'+i+'&nbsp;</span>'; 
						   
						}else{ 
							  
							  htmlStr += '<span class="num"><a href="javascript:doingList(collectionName,'+(i*length-(length-1))+','+length+');">'+i+'</a></span>';
						   
						}
						cnt = cnt + 1;
						//카운트가 10개되면 더이상 페이지 번호를 페이지에 찍지않는다.
						if (cnt == 10) { 
							 currMaxPageNum = i;
							 break;		   
						}
	
					}
					
					if ((cnt == 10) && ((i+1) <= maxPage)){  
	
						htmlStr += '<span class="num"><a href="javascript:doingList(collectionName,'+((currMaxPageNum+1)*length-(length-1))+','+length+');" >다음</a></span>';
					}else{
						htmlStr += '<span class="num">다음</span>';
					}
	
				
				
				htmlStr += '</div></div>';
			
				
				/////////////////////////////////////  paging ///////////////////////////////////

				
					
				$("#searchResult").html(htmlStr);
				var vvv = $("#pk").val();
				$("#searchDiv").html('<input type="text" class="inp02" id="pk" name="pk" value="'+vvv+'"/><input type="button" class="btn_c" value="찾기" onclick="doingSearch(currCollection,1,10,encodeURI($(\'#pk\').val()));" /> <input type="button" class="btn_c" value="취소" onclick="doingList(collectionName,1,5);" />');

			  }
		});
	}
	
	function getDatetime(){
		var currentTime = new Date()
		var month = currentTime.getMonth() + 1
		var day = currentTime.getDate()
		var year = currentTime.getFullYear()
		var hours = currentTime.getHours()
		var minutes = currentTime.getMinutes()
		var seconds = currentTime.getSeconds()
		if (month < 10){
			month = "0" + month
		}
		if (day < 10){
			day = "0" + day
		}
		if (hours < 10){
			hours = "0" + hours
		}
		if (minutes < 10){
			minutes = "0" + minutes
		}
		if (seconds < 10){
			seconds = "0" + seconds
		}
		
		return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
	}
	
	function goList(collection,length){
		var v = parseInt($('#gopage').val())*length-(length-1);
		doingList(collection,v,length);
	}
	
	function goSearch(){
	
	}
</script>
    
    
    
</head>

<body>
<div id="container">
<!-- header -->
<%@include file="../header.jsp" %>

<div id="sidebar">
	<div class="sidebox">
		<h3>테스트</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/search.jsp">검색테스트</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/analyzer.jsp">분석기테스트</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/dbTester.jsp">DB테스트</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/searchDoc.jsp" class="selected">문서원문조회</a></li>
			</ul>
	</div>
</div><!-- E : #sidebar -->


<div id="mainContent">

<h2>문서원문조회</h2>

<h4>결과요약</h4>
<div class="fbox">
<table class="tbl01">
	<colgroup><col width="25%" /><col width="" /></colgroup>
	<tbody>
	<tr>
	<th>컬렉션이름</th>
	<td style="text-align:left;padding-left:30px;">
		<span id="s_cn">
			<select id="chooseCollection" onchange="javascript:selectCollection(this)">
			<option value="none">---</option>
			<%
				for (int i = 0; i < colletionList.length; i++) {
					String col = colletionList[i];
					
			%>
				<option value="<%=col%>" ><%=col%></option>
				<%
					}
				%>
			</select>
		</span>
		
	</td>
	</tr>
	<tr>
	<th>문서아이디</th>
	<td style="text-align:left;padding-left:30px;">
			<div id="searchDiv"><input type="text" class="inp02" id="pk" name="pk" value=""/><input type="button" class="btn_c" value="찾기" onclick="alert($.URLEncode($('#pk').val()));doingSearch(currCollection,1,10,$.URLEncode($('#pk').val()));" /></div>
	</td>
	</tr>
	<tr>
	<th>세그먼트갯수</th>
	<td style="text-align:left;padding-left:30px;"><span id="s_seg">&nbsp;</span></td>
	</tr>
	<tr>
	<th>문서갯수</th>
	<td style="text-align:left;padding-left:30px;"><span id="s_doc">&nbsp;</span></td>
	</tr>
	</tbody>
</table>
</div>
			
<div id="searchResult">
			
</div>



				<!-- E : #mainContent -->
		</div>

	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->

</body>

</html>


