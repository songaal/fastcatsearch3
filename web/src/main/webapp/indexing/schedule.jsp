<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="java.text.SimpleDateFormat"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.settings.Schema"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.db.dao.IndexingSchedule"%>
<%@page import="org.fastcatsearch.db.vo.*"%>
<%@page import="org.fastcatsearch.ir.config.CollectionsConfig.*"%>

<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.Calendar"%>
<%@include file="../common.jsp" %>

<%
	/* IRConfig irConfig = IRSettings.getConfig(true);
	String collectinListStr = irConfig.getString("collection.list");
	String[] colletionList = collectinListStr.split(","); */
	
	IRService irService = ServiceManager.getInstance().getService(IRService.class);
	List<Collection> collectionList = irService.getCollectionList();
	
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	
	String cmd = WebUtils.getString(request.getParameter("cmd"), "");
	IndexingSchedule indexingSchedule = dbHandler.getDAO("IndexingSchedule");
	
	if(cmd.equals("1")){
		//apply indexing schedule
		
		String slcCollection = request.getParameter("slc_collection");
		String type = request.getParameter("type");
		
		int p_day = WebUtils.getInt(request.getParameter("p_day"), -1);
		int p_hour = WebUtils.getInt(request.getParameter("p_hour"), -1);
		int p_minute = WebUtils.getInt(request.getParameter("p_minute"), -1);
		
		String s_date = request.getParameter("s_date");
		int s_hour =  WebUtils.getInt(request.getParameter("s_hour"), -1);
		int s_minute =  WebUtils.getInt(request.getParameter("s_minute"), -1);
		
		//String isActiveStr =  WebUtils.getString(request.getParameter("isActive"), "0");
		boolean isActive = false;
		//if(isActiveStr.equals("1")){
		//	isActive = true;
		//}
		int period = p_day * 60 * 60 * 24 + p_hour * 60 * 60 + p_minute * 60;
		
		if(s_date != null && p_day>= 0 && p_hour >= 0 && s_minute >= 0 && s_hour >= 0 && s_minute >=0){
	String tmp  = s_date + " " + ((s_hour < 9) ? "0" + s_hour : s_hour) 
	+ ":" + ((s_minute < 9) ? "0" + s_minute : s_minute)  + ":00";
	
	//out.println(type+","+p_day+","+tmp);
	
	try{
		Timestamp startTime = Timestamp.valueOf(tmp);
		indexingSchedule.updateOrInsert(slcCollection, type, period, startTime, isActive);
		//dbHandler.commit();
	}catch(Exception e){
		e.printStackTrace();
	}
		}
		
		//stop scheduling
		JobService.getInstance().toggleIndexingSchedule(slcCollection, type, isActive);
		
	}else if(cmd.equals("2")){
		//toggle indexing schedule
		
		String slcCollection = request.getParameter("slc_collection");
		String type = request.getParameter("type");
		String isActiveStr =  WebUtils.getString(request.getParameter("isActive"), "0");
		boolean isActive = false;
		if(isActiveStr.equals("1")){
	isActive = true;
		}
		
		try{
	indexingSchedule.updateStatus(slcCollection, type, isActive);
	//dbHandler.commit();
		}catch(Exception e){
	e.printStackTrace();
		}
		
		//JobScheduler RELOAD!
		JobService.getInstance().toggleIndexingSchedule(slcCollection, type, isActive);
		
	}
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy,M,d");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="org.fastcatsearch.control.JobService"%><html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>FASTCAT 검색엔진 관리도구</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/reset.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/jquery-ui.css" rel="stylesheet" type="text/css" media="screen" />
<!--[if lte IE 6]>
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style_ie.css" rel="stylesheet" type="text/css" />
<![endif]-->
<!--[if lte IE 6]>
<link href="css/style_ie.css" rel="stylesheet" type="text/css" />
<![endif]-->
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/common.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-ui-1.8.9.min.js"></script>
<script>

function chkSelected (id)
{
	var tmp = document.getElementById ( id).options;
	
	for( i = 0; i < tmp.length; i++)
		if(tmp[i].selected == true)
			return ( tmp[i].value);
	return null;
}

function applyIndexingSchedule(type, id, isActive){

	if($("#p_day_"+type+"_"+id).val() == "" || $("#p_hour_"+type+"_"+id).val() == "" || $("#p_minute_"+type+"_"+id).val() == ""){
		alert("주기의 일/시/분을 설정하십시오.");
		return;
	}
	if($("#p_day_"+type+"_"+id).val() == "0" && $("#p_hour_"+type+"_"+id).val() == "0" && $("#p_minute_"+type+"_"+id).val() == "0"){
		alert("주기는 최소1분 이상이어야 합니다.");
		return;
	}
	if($("#s_date_"+type+"_"+id).val() == "" || $("#s_hour_"+type+"_"+id).val() == "" || $("#s_minute_"+type+"_"+id).val() == ""){
		alert("시작시각의 일/시/분을 설정하십시오.");
		return;
	}
	
	$("#cmd").val("1");
	$("#slc_collection").val($("#slc_collection_"+type+"_"+id).val());
	$("#type").val(type);
	$("#p_day").val($("#p_day_"+type+"_"+id).val());
	$("#p_hour").val($("#p_hour_"+type+"_"+id).val());
	$("#p_minute").val($("#p_minute_"+type+"_"+id).val());
	
	$("#s_date").val($("#s_date_"+type+"_"+id).val());
	$("#s_hour").val($("#s_hour_"+type+"_"+id).val());
	$("#s_minute").val($("#s_minute_"+type+"_"+id).val());
	$("#isActive").val(isActive);
	
	callAjax(type,id,isActive,"1");
	//$("#applyForm").submit();

}
function toggleIndexingSchedule(type, id, isActive){
	$("#cmd").val("2");
	$("#slc_collection").val($("#slc_collection_"+type+"_"+id).val());
	$("#type").val(type);
	$("#isActive").val(isActive);
	
	
	callAjax(type,id,isActive,"2");
	//$("#applyForm").submit();

}

function callAjax(type, id, isActive, cmd)
{	
	if ( cmd == "1" ) 
		{		
		$.ajax({
					url:"applySchedule.jsp",
					method:"post",
					async : true,
					data:{						
						slc_collection:id,type:type,cmd:cmd,
						p_day:$("#p_day_"+type+"_"+id).val(),
						p_hour:$("#p_hour_"+type+"_"+id).val(),
						p_minute:$("#p_minute_"+type+"_"+id).val(),
						s_date:$("#s_date_"+type+"_"+id).val(),
						s_hour:$("#s_hour_"+type+"_"+id).val(),
						s_minute:$("#s_minute_"+type+"_"+id).val()
						},
						success:function(responseText) {
						responseText=responseText.replace(/^\s\s*/,"");				
						console.log(responseText);						
						if (responseText.trim() == "0" ) 
							{
							$("#btn_"+type+"_"+id).removeClass("on");
							$("#btn_"+type+"_"+id).addClass("a.btn_s").addClass("off");
							$("#btn_"+type+"_"+id).text("OFF");
							$("#btn_"+type+"_"+id).attr("href","javascript:toggleIndexingSchedule('"+type+"', '"+id+"', 1)");
							}
						else
							{
								alert("색인 스케쥴링 정보 갱신에 실패했습니다.");
							}
						}					
				});
		}
	else if ( cmd == "2" )
	{
		$.ajax({
			url:"applySchedule.jsp",
			async : true,
			method:"post",
			data:{
				cmd:cmd,slc_collection:id,type:type,isActive:isActive
				},
			success:function(responseText) {
				responseText=responseText.replace(/^\s\s*/,"");				
				console.log(responseText+";");				
		
				if (responseText.trim() == "0" ) 
					{
					console.log("set On Button");				
					$("#btn_"+type+"_"+id).removeClass("off").addClass("on");
					$("#btn_"+type+"_"+id).text("ON");
					$("#btn_"+type+"_"+id).attr("href","javascript:toggleIndexingSchedule('"+type+"', '"+id+"', 0)");
					}
				else
					{
					console.log("set Off Button");									
					$("#btn_"+type+"_"+id).removeClass("on").addClass("off");
					$("#btn_"+type+"_"+id).text("OFF");
					$("#btn_"+type+"_"+id).attr("href","javascript:toggleIndexingSchedule('"+type+"', '"+id+"', 1)");					
					}			
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
		<h3>색인</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/main.jsp">색인정보</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/result.jsp">색인결과</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/schedule.jsp" class="selected">작업주기설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>indexing/history.jsp">색인히스토리</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>각 컬렉션에 대하여 색인주기를 설정합니다.</li>
			<li>각주기와 시작시각을 셋팅하고 저장버튼을 누르면 사용여부버튼이 활성화됩니다.</li>
			<li>사용여부가 ON이 되면 동작을 시작합니다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->


<div id="mainContent">
	<h2>색인주기설정</h2>
	<div class="fbox">
	
	<script type="text/javascript">		
	//<![CDATA[
		$(function() {
			<%
			for(int i = 0; i < colletionList.length; i++){
				String collection = colletionList[i];
				%>
				$( "#s_date_F_<%=collection%>").datepicker();
				$( "#s_date_F_<%=collection%>").datepicker("option", "dateFormat", "yy-mm-dd");
				$( "#s_date_I_<%=collection%>").datepicker();
				$( "#s_date_I_<%=collection%>").datepicker("option", "dateFormat", "yy-mm-dd");
				<%
				}
			%>
		});
	//]]>
	</script>
	
	<table summary="색인주기설정" class="tbl02">
	<thead>
	<tr>
		<th class="first">번호</th>
		<th>컬렉션이름</th>
		<th>색인종류</th>
		<th width="140">주기</th>
		<th width="180">시작시각</th>
		<th>저장</th>
		<th>사용여부</th>
	</tr>
	</thead>
	<tbody>
<%
	for(int i = 0; i < colletionList.length; i++){
		String collection = colletionList[i];		
		IndexingScheduleVO fullIndexingSchedule = indexingSchedule.select(collection, "F");
		IndexingScheduleVO incIndexingSchedule = indexingSchedule.select(collection, "I");
		
		if(!("".equals(collection) && colletionList.length==1)) {
%>

	<tr>
		<td rowspan="2" class="first"><%=i+1%></td>
		<td rowspan="2"><strong class="small tb"><%=collection%></strong></td>
		<td>전체색인</td>
		<td>
		<input type="hidden" id="slc_collection_F_<%=collection%>" value="<%=collection %>"/>
		<select name="p_day_F_<%=collection%>" id="p_day_F_<%=collection%>">
			<option value="0">일</option>
		<% 
		int period = -1;
		int pDay = -1;
		int pHour = -1;
		int pMinute = -1;
		long timestamp = -1;
		int sHour = -1;
		int sMinute = -1;
		Timestamp ts = null; 
		
		if(fullIndexingSchedule != null){
			period = fullIndexingSchedule.period;
			pDay = period/(3600*24);
			pHour = (period%(3600*24))/3600;
			pMinute = (period%3600)/60;
	
			ts = fullIndexingSchedule.startTime;
			timestamp = ts.getTime();
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(timestamp));

			sHour = cal.get(Calendar.HOUR_OF_DAY);
			sMinute = cal.get(Calendar.MINUTE);
		}else{
			timestamp = System.currentTimeMillis();
		}
		for ( int m = 0; m <= 29; m++){
			if ( pDay >= 0 && pDay == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
		<%
		}}
		for ( int m = 30; m <= 365; m+=10){
			if ( pDay >= 0 && pDay == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
		<%}}%>
		</select>
		<select name="p_hour_F_<%=collection%>" id="p_hour_F_<%=collection%>">
			<option value="">시</option>
			<% for ( int m = 0; m < 24; m++){
			if ( pHour >= 0 && pHour == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
		<%}}%>
		</select>
		<select name="p_minute_F_<%=collection%>" id="p_minute_F_<%=collection%>">
			<option value="">분</option>
			<% for ( int m = 0; m < 60; m++){
			if ( pMinute >= 0 && pMinute == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
		<%}}%>
		</select>
		</td>
		<td>
		<ul class="cfrm">
			<li>
			<input id="s_date_F_<%=collection%>" readonly name="s_date_F_<%=collection%>" type="text" class="inp03" size="7"/>
			<script>
			$(document).ready(function()
			{
				$("#s_date_F_<%=collection%>").val(
					$.datepicker.formatDate(
						 "yy-mm-dd", 
						 new Date(<%=timestamp %>)));
			});
			</script>
			</li>
			<li>
			<select id="s_hour_F_<%=collection%>" name="s_hour_F_<%=collection%>">
				<option value="">시</option>
				<% for ( int m = 0; m < 24; m++){
			if ( sHour >= 0 && sHour == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
			<%}}%>
			</select>
			<select id="s_minute_F_<%=collection%>" name="s_minute_F_<%=collection%>">
				<option value="">분</option>
				<% for ( int m = 0; m < 60; m++){
			if ( sMinute >= 0 && sMinute == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
			<%}}%>
			</select>
			</li>
		</ul>
		</td>
		<td><a href="javascript:applyIndexingSchedule('F', '<%=collection%>', 0)" class="btn_s">저장</a></td>
		<%if(fullIndexingSchedule != null){
			if(fullIndexingSchedule.isActive){
		%>
		<td><a id="btn_F_<%=collection%>" href="javascript:toggleIndexingSchedule('F', '<%=collection%>', 0)" class="btn_s on">ON</a></td>
		<%
			}else{
		%>
		<td><a id="btn_F_<%=collection%>" href="javascript:toggleIndexingSchedule('F', '<%=collection%>', 1)" class="btn_s off">OFF</a></td>
		<%	
			}
		}else{%>
		<td>OFF</td>
		<%} %>
		
		
	</tr>
	<tr>
		<td>증분색인</td>
		<%
		period = -1;
		pDay = -1;
		pHour = -1;
		pMinute = -1;
		timestamp = -1;
		sHour = -1;
		sMinute = -1;
		
		if(incIndexingSchedule != null){
			period = incIndexingSchedule.period;
			pDay = period/(3600*24);
			pHour = (period%(3600*24))/3600;
			pMinute = (period%3600)/60;
	
			ts = incIndexingSchedule.startTime;
			timestamp = ts.getTime();
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(timestamp));

			sHour = cal.get(Calendar.HOUR_OF_DAY);
			sMinute = cal.get(Calendar.MINUTE);
		}else{
			timestamp = System.currentTimeMillis();
		}
		%>
		<td>
		<input type="hidden" id="slc_collection_I_<%=collection%>" value="<%=collection %>"/>
		<select id="p_day_I_<%=collection%>" name="p_day_I_<%=collection%>">
			<option value="">일</option>
			<% for ( int m = 0; m < 31; m++){
			if ( pDay >= 0 && pDay == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
		<%}}%>
			</select>
		<select id="p_hour_I_<%=collection%>" name="p_hour_I_<%=collection%>">
			<option value="">시</option>
			<% for ( int m = 0; m < 24; m++){
			if ( pHour >= 0 && pHour == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
		<%}}%>
			</select>
		<select id="p_minute_I_<%=collection%>" name="p_minute_I_<%=collection%>">
			<option value="">분</option>
			<% for ( int m = 0; m < 60; m++){
			if ( pMinute >= 0 && pMinute == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
		<%}}%>
			</select>
		</td>
		<td>
		<ul class="cfrm">
			<li><input id="s_date_I_<%=collection%>" name="s_date_I_<%=collection%>" type="text" class="inp03" size="7" /></li>
			<script>
			$(document).ready(function()
			{
				 $("#s_date_I_<%=collection%>").val(
						 $.datepicker.formatDate(
								 "yy-mm-dd", 
								 new Date(<%=timestamp %>)));
			});
			</script>
			<li>
			<select id="s_hour_I_<%=collection%>" name="s_hour_I_<%=collection%>">
				<option value="">시</option>
				<% for ( int m = 0; m < 24; m++){
			if ( sHour >= 0 && sHour == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
		<%}}%>
			</select>
			<select id="s_minute_I_<%=collection%>" name="s_minute_I_<%=collection%>">
				<option value="">분</option>
				<% for ( int m = 0; m < 60; m++){
			if ( sMinute >= 0 && sMinute == m) {%>
			<option value="<%=m%>" selected><%=m%></option>
			<%} else { %>
			<option value="<%=m%>"><%=m%></option>
		<%}}%>
			</select>
			</li>
		</ul>
		</td>
		<td><a href="javascript:applyIndexingSchedule('I', '<%=collection%>', 0)" class="btn_s">저장</a></td>
		<%if(incIndexingSchedule != null){ 
			if(incIndexingSchedule.isActive){
		%>
		<td><a id="btn_I_<%=collection%>" href="javascript:toggleIndexingSchedule('I', '<%=collection%>', 0)" class="btn_s on">ON</a></td>
		<%
			}else{
		%>
		<td><a id="btn_I_<%=collection%>" href="javascript:toggleIndexingSchedule('I', '<%=collection%>', 1)" class="btn_s off">OFF</a></td>
		<%	
			}
		}else{%>
		<td>OFF</td>
		<%} %>
	</tr>
<%
		} else {
%>			
	<tr>
		<td colspan="7"> 컬렉션이 존재하지 않습니다. </td>
	</tr>
<%
		}
	}
%>
	</tbody>
	</table>
	</div>
	
	<!-- E : #mainContent --></div>
	
	
	<form name="applyForm" id="applyForm" action="" method="post">
		<input type="hidden" name="slc_collection" id="slc_collection" />
		<input type="hidden" name="type" id="type" />
		<input type="hidden" name="cmd" id="cmd" />
		<input type="hidden" name="p_day" id="p_day" />
		<input type="hidden" name="p_hour" id="p_hour" />
		<input type="hidden" name="p_minute" id="p_minute" />
		<input type="hidden" name="s_date" id="s_date" />
		<input type="hidden" name="s_hour" id="s_hour" />
		<input type="hidden" name="s_minute" id="s_minute" />
		<input type="hidden" name="isActive" id="isActive" />
	</form>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->


</body>

</html>
