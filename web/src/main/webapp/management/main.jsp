<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.control.JobService"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.service.KeywordService"%>
<%@page import="org.fastcatsearch.service.WebService"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.fastcatsearch.job.Job"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.management.ManagementInfoService"%>
<%@page import="org.fastcatsearch.server.CatServer"%>
<%@page import="org.fastcatsearch.statistics.StatisticsInfoService"%>
<%@page import="org.fastcatsearch.cluster.NodeService"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>

<%@page import="java.util.concurrent.ThreadPoolExecutor"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.Properties"%>

<%@page import="java.util.Date"%>
<%@page import="java.util.*"%>
<%@page import="java.net.URLDecoder"%>

<%@include file="../common.jsp" %>

<%
	String message = URLDecoder.decode(WebUtils.getString(request.getParameter("message"), ""),"utf-8");
	IRConfig irConfig = IRSettings.getConfig(true);
	Properties systemProps = System.getProperties();
	
	ThreadPoolExecutor executor = JobService.getInstance().getJobExecutor();
	long upTime = System.currentTimeMillis() - CatServer.startTime;
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
<script type="text/javascript">
	function alertMessage(){
		var message = "<%=message%>";
		if(message != "")
			alert(message);
	}

	function restartCatServer(){
		//managementService.jsp?cmd=3&component=0&cmd2=2
		var request = $.ajax({
		  url: "managementService.jsp",
		  type: "GET",
		  data: {cmd : "3", component : "0", cmd2 : "2"},
		  dataType: "html"
		});
		alert("서버를 재시작하였습니다.");
	}

	function restartServiceHandler(){
		//managementService.jsp?cmd=3&component=1&cmd2=2
		var request = $.ajax({
		  url: "managementService.jsp",
		  type: "GET",
		  data: {cmd : "3", component : "1", cmd2 : "2"},
		  dataType: "html"
		});
		alert("ServiceHandler를 재시작하였습니다.");
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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/main.jsp" class="selected">시스템상태</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/serverCluster.jsp">서버클러스터</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/searchEvent.jsp">이벤트내역</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/jobHistory.jsp">작업히스토리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/account.jsp">계정관리</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/config.jsp">사용자설정</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/advConfig.jsp">고급설정보기</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/restore.jsp">복원</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>management/license.jsp">라이선스</a></li>
			</ul>
	</div>
	<div class="sidebox">
		<h3>도움말</h3>
			<ul class="latest">
			<li>컴포넌트상태 : 각 컴포넌트를 시작/정지/재시작가능하다.</li>
			<li>시스템정보 : 검색엔진이 설치된 운영체제의 시스템정보를 보여주며, JVM의 정보를 확인할 수 있다.</li>
			<li>서버상태 : 구동시간과 사용하고 있는 JVM메모리를 보여준다.</li>
			<li>작업실행기 상태 : 실행중인 작업과 수행한 작업을 보여준다. POOL사이즈는 작업쓰레드의 갯수이다.</li>
			</ul>
	</div>
</div><!-- E : #sidebar -->

	<div id="mainContent">
	<h2>컴포넌트상태</h2>
	<div class="fbox">
	<table summary="색인결과" class="tbl02">
	<colgroup><col width="5%" /><col width="35%" /><col width="30%" /><col width="30%" /></colgroup>
	<thead>
	<tr>
		<th class="first">번호</th>
		<th>컴포넌트명</th>
		<th>상태</th>
		<th>동작</th>
	</tr>
	</thead>
	<tbody>
	<%
	
	
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		IRService irService = ServiceManager.getInstance().getService(IRService.class);
		WebService webService = ServiceManager.getInstance().getService(WebService.class);
		DBService dbService = ServiceManager.getInstance().getService(DBService.class);
		ManagementInfoService managementInfoService = ServiceManager.getInstance().getService(ManagementInfoService.class);
		StatisticsInfoService statisticsInfoService = ServiceManager.getInstance().getService(StatisticsInfoService.class);
		int no = 1;
		if(CatServer.getInstance() !=null) {
	%>
	<tr>
		<td class="first"><%=no++ %></td>
		<td>CatServer</td>
		<td><%=CatServer.getInstance().isRunning()? "실행중"  : "정지"%></td>
		<td>
		<a href="javascript:restartCatServer()" class="btn_s">재시작</a>&nbsp;
		</td>
	</tr>
	<%
		}
	%>
	<tr>
		<td class="first"><%=no++ %></td>
		<td>NodeService</td>
		<td><%=nodeService.isRunning()? "실행중"  : "-"%></td>
		<td>
			<%
				if(nodeService.isRunning()){
			%>
			<a href="managementService.jsp?cmd=3&component=10&cmd2=1" class="btn_s">정지</a>&nbsp;
			<a href="managementService.jsp?cmd=3&component=10&cmd2=2" class="btn_s">재시작</a>&nbsp;
			<%
				}else{
			%>
			<a href="managementService.jsp?cmd=3&component=10&cmd2=0" class="btn_s">시작</a>&nbsp;	
			<%
				}
			%>
		</td>
	</tr>
	<tr>
		<td class="first"><%=no++ %></td>
		<td>IRService</td>
		<td><%=irService.isRunning()? "실행중"  : "-"%></td>
		<td>
			<%
				if(irService.isRunning()){
			%>
			<a href="managementService.jsp?cmd=3&component=2&cmd2=1" class="btn_s">정지</a>&nbsp;
			<a href="managementService.jsp?cmd=3&component=2&cmd2=2" class="btn_s">재시작</a>&nbsp;
			<%
				}else{
			%>
			<a href="managementService.jsp?cmd=3&component=2&cmd2=0" class="btn_s">시작</a>&nbsp;	
			<%
				}
			%>
		</td>
	</tr>
	<tr>
		<td class="first"><%=no++ %></td>
		<td>WebService</td>
		<td><%=webService.isRunning()? "실행중"  : "-"%></td>
		<td>
			<a href="javascript:restartServiceHandler()" class="btn_s">재시작</a>&nbsp;
		</td>
	</tr>
	<tr>
		<td class="first"><%=no++ %></td>
		<td>DBService</td>
		<td><%=dbService.isRunning()? "실행중"  : "-"%></td>
		<td>
			<%
				if(dbService.isRunning()){
			%>
			<a href="managementService.jsp?cmd=3&component=3&cmd2=1" class="btn_s">정지</a>&nbsp;
			<a href="managementService.jsp?cmd=3&component=3&cmd2=2" class="btn_s">재시작</a>&nbsp;
			<%
				}else{
			%>
			<a href="managementService.jsp?cmd=3&component=3&cmd2=0" class="btn_s">시작</a>&nbsp;		
			<%
						}
					%>
		</td>
	</tr>
	<tr>
		<td class="first"><%=no++ %></td>
		<td>JobService</td>
		<td><%=JobService.getInstance().isRunning()? "실행중"  : "-"%></td>
		<td>
			<%
				if(JobService.getInstance().isRunning()){
			%>
			<a href="managementService.jsp?cmd=3&component=4&cmd2=1" class="btn_s">정지</a>&nbsp;
			<a href="managementService.jsp?cmd=3&component=4&cmd2=2" class="btn_s">재시작</a>&nbsp;
			<%
				}else{
			%>
			<a href="managementService.jsp?cmd=3&component=4&cmd2=0" class="btn_s">시작</a>&nbsp;	
			<%
					}
				%>
		</td>
	</tr>
	<%-- <tr>
		<td class="first"><%=no++ %></td>
		<td>KeywordService</td>
		<td><%=KeywordService.getInstance().isRunning()? "실행중"  : "-"%></td>
		<td>
			<%
				if(KeywordService.getInstance().isRunning()){
			%>
			<a href="managementService.jsp?cmd=3&component=5&cmd2=1" class="btn_s">정지</a>&nbsp;
			<a href="managementService.jsp?cmd=3&component=5&cmd2=2" class="btn_s">재시작</a>&nbsp;
			<%
				}else{
			%>
			<a href="managementService.jsp?cmd=3&component=5&cmd2=0" class="btn_s">시작</a>&nbsp;
			<%
				}
			%>
		</td>
	</tr> --%>
	<%-- <tr>
		<td class="first"><%=no++ %></td>
		<td>IRClassLoader</td>
		<td><%=IRClassLoader.getInstance().isRunning()? "실행중"  : "-"%></td>
		<td>
			<%
				if(IRClassLoader.getInstance().isRunning()){
			%>
			<a href="managementService.jsp?cmd=3&component=7&cmd2=1" class="btn_s">정지</a>&nbsp;
			<a href="managementService.jsp?cmd=3&component=7&cmd2=2" class="btn_s">재시작</a>&nbsp;
			<%
				}else{
			%>
			<a href="managementService.jsp?cmd=3&component=7&cmd2=0" class="btn_s">시작</a>&nbsp;	
			<%
					}
				%>
		</td>
	</tr> --%>
	<tr>
		<td class="first"><%=no++ %></td>
		<td>ManagementInfoService</td>
		<td><%=managementInfoService.isRunning()? "실행중"  : "-"%></td>
		<td>
			<%
				if(managementInfoService.isRunning()){
			%>
			<a href="managementService.jsp?cmd=3&component=8&cmd2=1" class="btn_s">정지</a>&nbsp;
			<a href="managementService.jsp?cmd=3&component=8&cmd2=2" class="btn_s">재시작</a>&nbsp;
			<%
				}else{
			%>
			<a href="managementService.jsp?cmd=3&component=8&cmd2=0" class="btn_s">시작</a>&nbsp;	
			<%
					}
				%>
		</td>
	</tr>
	<tr>
		<td class="first"><%=no++ %></td>
		<td>StatisticsInfoService</td>
		<td><%=statisticsInfoService.isRunning()? "실행중"  : "-"%></td>
		<td>
			<%
				if(statisticsInfoService.isRunning()){
			%>
			<a href="managementService.jsp?cmd=3&component=9&cmd2=1" class="btn_s">정지</a>&nbsp;
			<a href="managementService.jsp?cmd=3&component=9&cmd2=2" class="btn_s">재시작</a>&nbsp;
			<%
				}else{
			%>
			<a href="managementService.jsp?cmd=3&component=9&cmd2=0" class="btn_s">시작</a>&nbsp;	
			<%
					}
				%>
		</td>
	</tr>
	</tbody>
	</table>
	</div>
	
			
	<h2>시스템정보</h2>
	<div class="fbox">
	<table summary="시스템상태" class="tbl01">
	<colgroup><col width="20%" /><col width="" /></colgroup>
	<tbody>
	<tr>
		<th>검색엔진 홈</th>
		<td><%=IRSettings.HOME%></td>
	</tr>
	<tr>
		<th>시스템시각</th>
		<td><%=new Timestamp(System.currentTimeMillis())%></td>
	</tr>
	<tr>
		<th>JDK 벤더/버전</th>
		<td><%=systemProps.getProperty("java.vendor")%><br/>
				<%=systemProps.getProperty("java.vm.name")%><br/>
				<%=systemProps.getProperty("java.version")%></td>
	</tr>
	<tr>
		<th>JAVA HOME</th>
		<td><%=systemProps.getProperty("java.home")%></td>
	</tr>
	<tr>
		<th>클래스패스</th>
		<td><textarea style="width:100%" rows="3" readonly="true"><%=systemProps.getProperty("java.class.path")%></textarea></td>
	</tr>
	<tr>
		<th>운영체제</th>
		<td><%=systemProps.getProperty("os.name")%> (<%=systemProps.getProperty("os.arch")%>) <%=systemProps.getProperty("os.version")%></td>
	</tr>
		<tr>
		<th>파일구분자</th>
		<td><%=systemProps.getProperty("file.separator")%></td>
	</tr>
		<tr>
		<th>경로구분자</th>
		<td><%=systemProps.getProperty("path.separator")%></td>
	</tr>
	<tr>
		<th>줄바꿈문자</th>
		<td><%=systemProps.getProperty("line.separator").replaceAll("\\n","&#92;n").replaceAll("\\r","&#92;r")%></td>
	</tr>
	<tr>
		<th>사용자이름</th>
		<td><%=systemProps.getProperty("user.name")%></td>
	</tr>
	</tbody>
	</table>
	</div>
	
	<div class="boxL">
		<h4>서버상태</h4>
		<div class="fbox">
		<table summary="서버상태" class="tbl01">
		<colgroup><col width="40%" /><col width="" /></colgroup>
		<tbody>
		<!-- tr>
			<th>클라이언트 수</th>
			<td><%=webService.getClientCount()%></td>
		</tr-->
		<tr>
			<th>구동시간</th>
			<td><%=Formatter.getFormatTime(upTime)%></td>
		</tr>
		<tr class="last">
			<th>사용메모리</th>
			<td><%=Formatter.getFormatSize(Runtime.getRuntime().totalMemory())%></td>
		</tr>
		</tbody>
		</table>
		</div>
	</div>
	
	<div class="boxR">
		<h4>작업실행기 상태</h4>
		<div class="fbox">
		<table summary="데이터소스 설정" class="tbl01">
		<colgroup><col width="40%" /><col width="" /></colgroup>
		<tbody>
		<tr>
			<th>실행중</th>
			<td><%=executor.getActiveCount()%>
			<%
				if(executor.getActiveCount() > 0){
					Iterator<Job> iter = JobService.getInstance().getRunningJobs().iterator();
					while(iter.hasNext()){
						Job job = iter.next();
						String argStr = "";
						Object obj = job.getArgs();
						if(obj != null){
							if(obj instanceof String){
								argStr = obj.toString();
							}else if(obj instanceof String[]){
								String[] sa = (String[])obj;
								for(int i=0;i < sa.length;i++){
									argStr += sa[i].toString();
									if(i < sa.length - 1){
										argStr += " / ";
									}
								}
							}else{
								argStr = obj.toString();
							}
						}else{
							argStr = "";
						}
			%>
					<br/><%=job.getClass() %> / <%=argStr %>	
					<%
				}
			}//if(executor.getActiveCount() > 0){
			%>
			
			</td>
		</tr>
		<tr>
			<th>POOL 사이즈</th>
			<td><%=executor.getPoolSize() %></td>
		</tr>
		<tr>
			<th>최대 POOL 사이즈</th>
			<td><%=executor.getMaximumPoolSize() %></td>
		</tr>
		<tr class="last">
			<th>수행한 작업수</th>
			<td><%=executor.getCompletedTaskCount() %></td>
		</tr>
		</tbody>
		</table>
		</div>
	</div>
	<p class="clear"></p>
	<!-- E : #mainContent --></div>

<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->

</body>

</html>
