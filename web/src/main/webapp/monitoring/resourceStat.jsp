<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="java.util.Calendar"%>
<%@page import="org.fastcatsearch.db.dao.SearchEvent"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="java.net.URLConnection"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.db.dao.JobHistory"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.log.EventDBLogger"%>
<%@page import="java.util.*"%>
<%@page import="java.net.URLDecoder"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@include file="../common.jsp" %>
<%
	String cmd = request.getParameter("cmd");
	String type = request.getParameter("type");
	String collection = WebUtils.getString(request.getParameter("collection"),"");
	if(type == null){
		type = "hour";
	}
	String message = "";
	if ("login".equals(cmd)) {
		String username = request.getParameter("username");
		String passwd = request.getParameter("passwd");
		String[] accessLog = IRSettings.isCorrectPasswd(username, passwd);
		if(accessLog != null){
			//로긴 성공
			session.setAttribute("authorized", username);
			session.setAttribute("lastAccessLog", accessLog);
			session.setMaxInactiveInterval(60 * 30); //30 minutes
			IRSettings.storeAccessLog(username, ""); //ip주소는 공란으로 남겨두고 사용하지 않도록함. 
			//request.getRemoteAddr()로는 제대로된 사용자 ip를 알아낼수 없음.
			//jetty에서는 getHeader("REMOTE_ADDR"); 또는 req.getHeaer("WL-Proxy-Client-IP")+","+req.getHeaer("Proxy-Client-IP")+","+req.getHeaer("X-Forwarded-For")) 등을 제공하지 않는다.
			message = "";
		}else{
			message = "아이디와 비밀번호를 확인해주세요.";
		}
		
	}else if ("logout".equals(cmd)) {
		session.invalidate();
		response.sendRedirect(FASTCAT_MANAGE_ROOT+"index.jsp");
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>FASTCAT 검색엔진 관리도구</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/reset.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style.css" rel="stylesheet" type="text/css" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/jquery-ui.css" rel="stylesheet" type="text/css" media="screen" />
<link href="<%=FASTCAT_MANAGE_ROOT%>css/ui.jqgrid.css" type="text/css" rel="stylesheet" />

<script src="<%=FASTCAT_MANAGE_ROOT%>js/amcharts/amcharts.js" type="text/javascript"></script>
<script src="<%=FASTCAT_MANAGE_ROOT%>js/amcharts/raphael.js" type="text/javascript"></script>
<!--[if lte IE 6]>
<link href="<%=FASTCAT_MANAGE_ROOT%>css/style_ie.css" rel="stylesheet" type="text/css" />
<![endif]-->
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/common.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery-ui-1.8.9.min.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/calendar.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/resourceStat.js"></script>
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/grid.locale-en.js"></script> 
<script type="text/javascript" src="<%=FASTCAT_MANAGE_ROOT%>js/jquery.jqGrid.min.js"></script>
<script>
	var typeTag = "<%=type%>";
	$(document).ready(function() {
		var message = "<%=message %>";
		if(message != "")
			alert(message);
	});

	function logout(){
		location.href="?cmd=logout";
	}
	$(document).ready(function(){
			if(typeTag == "hour"){
				$('#showType option:eq(0)').attr('selected', true);
			}else if(typeTag == "day"){
				$('#showType option:eq(1)').attr('selected', true);
			}else if(typeTag == "week"){
				$('#showType option:eq(2)').attr('selected', true);
			}else if(typeTag == "month"){
				$('#showType option:eq(3)').attr('selected', true);
			}else if(typeTag == "year"){
				$('#showType option:eq(4)').attr('selected', true);
			}
			$( "#selectDate" ).datepicker({
 				onSelect: function(dateText, inst) {
 					//changeSelect();  
 					if(typeTag == "week")
 						$("#selectWeek").html(week(dateText+" 00:00:00")[0].substring(0,10)+"~"+week(dateText+" 00:00:00")[1].substring(0,10));
 					init();
			 	}
			});
			$( "#selectDate" ).datepicker("option", "dateFormat", "yy-mm-dd");
			$("#selectDate").val($.datepicker.formatDate("yy-mm-dd",new Date()));
			
			$( "#selectDate_2" ).datepicker({
 				onSelect: function(dateText, inst) {
	 				if(typeTag == "week")
	 				   $("#hiddenCompare_3").html(week(dateText+" 00:00:00")[0].substring(0,10)+"~"+week(dateText+" 00:00:00")[1].substring(0,10));
 					changeSelect(); 
			 	}
		});
		 $( "#selectDate_2" ).datepicker("option", "dateFormat", "yy-mm-dd");
		 $("#selectDate_2").val("0000-00");
		 
		 $( "#selectDate_3" ).datepicker({
 			onSelect: function(dateText, inst) {
	 			if(typeTag == "week")
	 				$("#hiddenCompare_7").html(week(dateText+" 00:00:00")[0].substring(0,10)+"~"+week(dateText+" 00:00:00")[1].substring(0,10));
 				changeSelect(); 
			}
		});
		$( "#selectDate_3" ).datepicker("option", "dateFormat", "yy-mm-dd");
		$("#selectDate_3").val("0000-00");
		
	});
			
</script>
</head>
<body>
<div id="container">
<!-- header -->
<%@include file="../header.jsp" %>
<div id="sidebar">
	<div class="sidebox">
		<h3>통계</h3>
			<ul class="latest">
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>monitoring/resourceStat.jsp" class="selected">리소스통계</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>monitoring/searchStat.jsp">검색통계</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>통계</h3>
			<ul class="latest">
			<li>검색엔진 통계에 대한 정보입니다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->	
<div id="mainContent">
		<div style="float:left;width:100%;height:30px;">
			<a href="javascript:changeShowType('hour');" id="a_hour" class="btn_s">시간</a>
			<a href="javascript:changeShowType('day');" id="a_day" class="btn_s">일간</a>
			<a href="javascript:changeShowType('week');" id="a_week" class="btn_s">주간</a>
			<a href="javascript:changeShowType('month');" id="a_month" class="btn_s">월간</a>
			<a href="javascript:changeShowType('year');" id="a_year" class="btn_s">연간</a>
		</div>
		<%if("day".equals(type)){%>
			<table summary="색인주기설정" class="tbl05">
			<tbody >
				<tr>
					<td width="90"></td>
					<td width="90"></td>
					<td width="30"></td>
				</tr>
				<tr>
					<td>
						<span>기준날짜선택</span>
					</td>
					<td>
						<input id="selectDate" readonly name="selectDate" type="text" class="inp03" size="10"/>
					</td>
					<td>
						<a class="btn_color_0" >&nbsp;&nbsp;&nbsp;</a>
					</td>
				</tr>
				<tr>
					<td>
						<span  id="hiddenCompare_1" style="display:none;">비교날짜선택</span>
						<span  id="hiddenCompare_0" style="display:block;">
							<a class="btn_s"  id="btn_show" onclick="showHiddenHour();">비교추가</a>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_2" style="display:none;">
							<input id="selectDate_2" readonly name="selectDate_2" type="text" class="inp03" size="10"/>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_4" style="display:none;">
							<a class="btn_color_1" >&nbsp;&nbsp;&nbsp;</a>
						</span>
					</td>
				</tr>
				<tr>
					<td>
					<span  id="hiddenCompare_5" style="display:none;">
							<a class="btn_s"  id="btn_show" onclick="showHiddenHour();">비교추가</a>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_6" style="display:none;">
							<input id="selectDate_3" readonly name="selectDate_3" type="text" class="inp03" size="10"/>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_8" style="display:none;">
							<a class="btn_color_2" >&nbsp;&nbsp;&nbsp;</a>
						</span>
					</td>
				</tr>
				</tbody>
				</table>
			<%}else if("week".equals(type)){ %>
				<table summary="색인주기설정" class="tbl07">
			<tbody >
				<tr>
					<td width="90"></td>
					<td width="90"></td>
					<td width="180"></td>
					<td width="30"></td>
				</tr>
				<tr>
					<td>
						<span>기준날짜선택</span>
					</td>
					<td>
						<input id="selectDate" readonly name="selectDate" type="text" class="inp03" size="10"/>
					</td>
					<td>
						<span  id="selectWeek" >0000 00-00~0000 00-00</span>
					</td>
					<td>
						<a class="btn_color_0" >&nbsp;&nbsp;&nbsp;</a>
					</td>
				</tr>
				<tr>
					<td>
						<span  id="hiddenCompare_1" style="display:none;">비교날짜선택</span>
						<span  id="hiddenCompare_0" style="display:block;">
							<a class="btn_s"  id="btn_show" onclick="showHiddenHour();">비교추가</a>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_2" style="display:none;">
							<input id="selectDate_2" readonly name="selectDate_2" type="text" class="inp03" size="10"/>
						</span>
					</td>
					<td>
						<span id="hiddenCompare_3" style="display:none;">0000 00-00~0000 00-00</span>
					</td>
					<td>
						<span  id="hiddenCompare_4" style="display:none;">
							<a class="btn_color_1" >&nbsp;&nbsp;&nbsp;</a>
						</span>
					</td>
				</tr>
				<tr>
					<td>
					<span  id="hiddenCompare_5" style="display:none;">
							<a class="btn_s"  id="btn_show" onclick="showHiddenHour();">비교추가</a>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_6" style="display:none;">
							<input id="selectDate_3" readonly name="selectDate_3" type="text" class="inp03" size="10"/>
						</span>
					</td>
					<td>
						<span id="hiddenCompare_7" style="display:none;">0000 00-00~0000 00-00</span>
					</td>
					<td>
						<span  id="hiddenCompare_8" style="display:none;">
							<a class="btn_color_2" >&nbsp;&nbsp;&nbsp;</a>
						</span>
					</td>
				</tr>
				</tbody>
				</table>
			<%}else if("month".equals(type)){ %>
				<table summary="색인주기설정" class="tbl06">
			<tbody >
				<tr>
					<td width="90"></td>
					<td width="90"></td>
					<td width="30"></td>
					<td width="30"></td>
				</tr>
				<tr>
					<td>
						<span>기준날짜선택</span>
					</td>
					<td>
						<select name="selectYear_0" id="selectYear_0" onchange="init();">	
						<%
						Calendar cal = Calendar.getInstance();
						for(int i=cal.get(Calendar.YEAR); i>cal.get(Calendar.YEAR)-3; i--){
							%>
							<option value="<%=i%>" ><%=i%></option>
							<%
						}
						%>
						</select>
					</td>
					<td>
						<select name="selectMonth_0" id="selectMonth_0" onchange="init();">	
						<%
						for(int i=1; i<13; i++){
							%>
							<option value="<%=i%>" ><%=i%></option>
							<%
						}
						%>
						</select>
					</td>
					<td>
						<a class="btn_color_0" >&nbsp;&nbsp;&nbsp;</a>
					</td>
				</tr>
				<tr>
					<td>
						<span  id="hiddenCompare_1" style="display:none;">비교날짜선택</span>
						<span  id="hiddenCompare_0" style="display:block;">
							<a class="btn_s"  id="btn_show" onclick="showHiddenHour();">비교추가</a>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_2" style="display:none;">
							<select name="selectYear_1" id="selectYear_1" onchange="changeSelect();">	
							<option >년</option>
							<%
							for(int i=cal.get(Calendar.YEAR); i>cal.get(Calendar.YEAR)-3; i--){
								%>
								<option value="<%=i%>" ><%=i%></option>
								<%
							}
							%>
							</select>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_3" style="display:none;">
						<select name="selectMonth_1" id="selectMonth_1" onchange="changeSelect();">		
						<option >월</option>
						<%
						for(int i=1; i<13; i++){
							%>
							<option value="<%=i%>" ><%=i%></option>
							<%
						}
						%>
						</select>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_4" style="display:none;">
							<a class="btn_color_1" >&nbsp;&nbsp;&nbsp;</a>
						</span>
					</td>
				</tr>
				<tr>
					<td>
						<span  id="hiddenCompare_5" style="display:none;">
							<a class="btn_s"  id="btn_show" onclick="showHiddenHour();">비교추가</a>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_6" style="display:none;">
							<select name="selectYear_2" id="selectYear_2" onchange="changeSelect();">	
							<option >년</option>
							<%
							for(int i=cal.get(Calendar.YEAR); i>cal.get(Calendar.YEAR)-3; i--){
								%>
								<option value="<%=i%>" ><%=i%></option>
								<%
							}
							%>
							</select>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_7" style="display:none;">
						<select name="selectMonth_2" id="selectMonth_2" onchange="changeSelect();">	
						<option >월</option>	
						<%
						for(int i=1; i<13; i++){
							%>
							<option value="<%=i%>" ><%=i%></option>
							<%
						}
						%>
						</select>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_8" style="display:none;">
							<a class="btn_color_2" >&nbsp;&nbsp;&nbsp;</a>
						</span>
					</td>
				</tr>
				</tbody>
				</table>
			<%}else if("year".equals(type)){ %>
			<table summary="색인주기설정" class="tbl03">
			<tbody >
				<tr>
					<td width="90"></td>
					<td width="40"></td>
					<td width="30"></td>
				</tr>
				<tr>
					<td>
						<span>기준날짜선택</span>
					</td>
					<td>
						<select name="selectYear_0" id="selectYear_0" onchange="init();">	
						<%
						Calendar cal = Calendar.getInstance();
						for(int i=cal.get(Calendar.YEAR); i>cal.get(Calendar.YEAR)-3; i--){
							%>
							<option value="<%=i%>" ><%=i%></option>
							<%
						}
						%>
						</select>
					</td>
					<td>
						<a class="btn_color_0" >&nbsp;&nbsp;&nbsp;</a>
					</td>
				</tr>
				<tr>
					<td>
						<span  id="hiddenCompare_1" style="display:none;">비교날짜선택</span>
						<span  id="hiddenCompare_0" style="display:block;">
							<a class="btn_s"  id="btn_show" onclick="showHiddenHour();">비교추가</a>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_2" style="display:none;">
							<select name="selectYear_1" id="selectYear_1" onchange="changeSelect();">	
							<option >년</option>
							<%
							for(int i=cal.get(Calendar.YEAR); i>cal.get(Calendar.YEAR)-3; i--){
								%>
								<option value="<%=i%>" ><%=i%></option>
								<%
							}
							%>
							</select>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_4" style="display:none;">
							<a class="btn_color_1" >&nbsp;&nbsp;&nbsp;</a>
						</span>
					</td>
				</tr>
				<tr>
					<td>
					<span  id="hiddenCompare_5" style="display:none;">
							<a class="btn_s"  id="btn_show" onclick="showHiddenHour();">비교추가</a>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_6" style="display:none;">
							<select name="selectYear_2" id="selectYear_2" onchange="changeSelect();">	
							<option >년</option>
							<%
							for(int i=cal.get(Calendar.YEAR); i>cal.get(Calendar.YEAR)-3; i--){
								%>
								<option value="<%=i%>" ><%=i%></option>
								<%
							}
							%>
							</select>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_8" style="display:none;">
							<a class="btn_color_2" >&nbsp;&nbsp;&nbsp;</a>
						</span>
					</td>
				</tr>
				</tbody>
				</table>
			<%}else{ %>
			<table summary="색인주기설정" class="tbl04">
			<tbody >
				<tr>
					<td width="90">
					</td>
					<td width="90">
					</td>
					<td width="30">
					</td>
					<td width="30">
					</td>
				</tr>
				<tr>
					<td>
						<span>기준날짜선택</span>
					</td>
					<td>
						<input id="selectDate" readonly name="selectDate" type="text" class="inp03" size="10"/>
					</td>
					<td>
						<select name="selectHour" id="selectHour" onchange="init();">	
						<%
						for(int i=0; i<24; i++){
							%>
							<option value="<%=i%>" ><%=i%></option>
							<%
						}
						%>
						</select>
					</td>
					<td>
						<a class="btn_color_0" >&nbsp;&nbsp;&nbsp;</a>
					</td>
				</tr>
				<tr>
					<td>
						<span  id="hiddenCompare_1" style="display:none;">비교날짜선택</span>
						<span  id="hiddenCompare_0" style="display:block;">
							<a class="btn_s"  id="btn_show" onclick="showHiddenHour();">비교추가</a>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_2" style="display:none;">
							<input id="selectDate_2" readonly name="selectDate_2" type="text" class="inp03" size="10"/>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_3" style="display:none;">
						<select name="selectHour_2" id="selectHour_2" onchange="changeSelect();">		
						<option >시</option>
						<%
						for(int i=0; i<24; i++){
							%>
							<option value="<%=i%>" ><%=i%></option>
							<%
						}
						%>
						</select>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_4" style="display:none;">
							<a class="btn_color_1" >&nbsp;&nbsp;&nbsp;</a>
						</span>
					</td>
				</tr>
				<tr>
					<td>
						<span  id="hiddenCompare_5" style="display:none;">
							<a class="btn_s"  id="btn_show" onclick="showHiddenHour();">비교추가</a>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_6" style="display:none;">
							<input id="selectDate_3" readonly name="selectDate_3" type="text" class="inp03" size="10"/>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_7" style="display:none;">
						<select name="selectHour_3" id="selectHour_3" onchange="changeSelect();">	
						<option >시</option>	
						<%
						for(int i=0; i<24; i++){
							%>
							<option value="<%=i%>" ><%=i%></option>
							<%
						}
						%>
						</select>
						</span>
					</td>
					<td>
						<span  id="hiddenCompare_8" style="display:none;">
							<a class="btn_color_2" >&nbsp;&nbsp;&nbsp;</a>
						</span>
					</td>
				</tr>
				</tbody>
				</table>
				<%} %>
		<div style="width:100%;height:30px;padding:20px;">
			<a href="javascript:changeTab('cpu');" id="a_cpu" class="btn_s">CPU</a>
			<a href="javascript:changeTab('mem');" id="a_mem" class="btn_s">메모리</a>
			<a href="javascript:changeTab('load');" id="a_load" class="btn_s">서버부하</a>
		</div>
		<div id="graphDiv" style="width:100%;height:300px;">
			<div id="cpuDiv" class="chartContainer">
				<div id="chartJCPUDiv" class="chartDiv"></div>
			</div>
			<div id="memDiv" class="chartContainer">
				<div id="chartMemDiv" class="chartDiv"></div>
			</div>
			<div id="loadDiv" class="chartContainer">
				<div id="chartLoadDiv" class="chartDiv"></div>
			</div>
		</div>
		<div style="width:100%;height:30px;">
			<a href="javascript:downloadCSV();" class="btn_s">CSV</a>
		</div>
		<div class="fbox" >
		<table class="tbl02" >
			<colgroup>
				<col width="120px" />
				<col width="120px" />
				<col width="120px" />
				<col width="120px" />
				<col width="120px" />
				<col id="socol" width="138px" />
			</colgroup>
			<thead>
				<tr>
					<th>시간</th>
					<th id="stime"></th>
					<th id="ctime_1"></th>
					<th id="rate_1">비교시간1<br>증감율</th>
					<th id="ctime_2"></th>
					<th id="rate_2">비교시간2<br>증감율</th>
				</tr>
			</thead>
		</table>
		<div id="tbDiv" style="overflow: -moz-scrollbars-vertical;overflow-y:scroll; width:100%;">
		<table id='searchResult' class="tbl02" >
			<colgroup>
				<col width="120px" />
				<col width="120px" />
				<col width="120px" />
				<col width="120px" />
				<col width="120px" />
				<col width="120px" />
			</colgroup>
			<tbody id="dataTable">
			</tbody>
		</table>
		</div>
		</div>
<!-- E : #mainContent -->
</div>

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->
<form action="/monitoring/csv" method="post" name="csvForm" style="display:none">
	<input type=hidden name="data" value="">
	<input type=hidden name="filename" value="">
</form>
</body>
</html>
