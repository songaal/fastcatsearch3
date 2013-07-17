<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="java.io.FileWriter"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="java.io.File"%>
<%@page import="java.io.FileWriter"%>
<%@page import="java.io.IOException"%>
<%@page import="java.util.Properties"%>
<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.fastcatsearch.job.IncIndexJob"%>
<%@page import="org.fastcatsearch.job.FullIndexJob"%>
<%@page import="org.fastcatsearch.control.JobService"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.management.ManagementInfoService"%>
<%@page import="org.fastcatsearch.statistics.StatisticsInfoService"%>
<%@page import="org.fastcatsearch.license.LicenseInfo"%>
<%@page import="org.fastcatsearch.server.CatServer"%>
<%@page import="org.fastcatsearch.cluster.NodeService"%>
<%@page import="org.fastcatsearch.license.*"%>

<%@include file="../common.jsp" %>

<%
	int cmd = Integer.parseInt(request.getParameter("cmd"));
String collection = request.getParameter("collection");

switch(cmd){
	case 0:
	{
		break;
	}
	case 1:
		// 사용자설정을 저장한다.
	{
		//save fastcat.conf
		//Properties props = new Properties();
		//String[] keyList = {"collection.list","jobExecutor.core.poolsize","jobExecutor.max.poolsize","jobExecutor.keepAliveTime"
		//		,"pk.term.interval","pk.bucket.size","document.read.buffer.size","document.write.buffer.size","document.block.size"
		//		,"document.compression.type","index.term.interval","index.work.bucket.size","index.work.memory","index.work.check"
		//		,"data.sequence.cycle","search.highlightAndSummary","segment.separate.add.indexing","segment.document.limit"
		//		,"segment.revision.backup.size","server.admin.path","server.port","server.logs.dir","korean.dic.path","user.dic.path"
		//		,"synonym.dic.path","synonym.two-way","stopword.dic.path","specialCharacter.map.path","dynamic.classpath"};
		String[] keyList = {"server.port","synonym.two-way","dynamic.classpath"};
		IRConfig irConfig = IRSettings.getConfig();
		Properties props = irConfig.getProperties();
		for(int i =0;i<keyList.length ;i++){
	String tmp = WebUtils.getString(request.getParameter(keyList[i]), "");
	props.setProperty(keyList[i], tmp);
		}
		
		IRSettings.storeConfig(irConfig);

		response.sendRedirect("config.jsp?message="+URLEncoder.encode("저장하였습니다.", "utf-8"));
		
		break;
	}
	
	case 2:
	{
		//RESTORE
    	int segmentNumber = Integer.parseInt(request.getParameter("segment"));
    	int revisioinNumber = Integer.parseInt(request.getParameter("revision"));
    	
    	CollectionHandler handler = IRService.getInstance().collectionHandler(collection);
    	try {
	boolean isSuccess = handler.restore(segmentNumber, revisioinNumber);
	
	if(isSuccess){
		response.sendRedirect("restore.jsp?message="+URLEncoder.encode("컬렉션 "+collection+"을 성공적으로 복원하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("restore.jsp?message="+URLEncoder.encode("이전시점의 세그먼트/리비전 번호를 지정해주세요.", "utf-8"));
	}
		} catch (Exception e) {
	response.sendRedirect("restore.jsp?message="+URLEncoder.encode("컬렉션 "+collection+" 복원시 에러발생! "+e.getMessage(), "utf-8"));
		} 
    	
		break;
	}
	
	//컴포넌트 정지
	case 3:
	{
		int component = Integer.parseInt(request.getParameter("component"));
		int cmd2 = Integer.parseInt(request.getParameter("cmd2"));
		//
		//cmd2 := 0 시작, 1 정지, 2 재시작
		
		if(cmd2 == 0){
	//시작
	if(component == 0){
		//CatServer
		if(CatServer.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("CatServer가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(CatServer.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("CatServer 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("CatServer 시작에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 1){
		//ServiceHandler
		if(WebService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("ServiceHandler가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(WebService.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("ServiceHandler 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("ServiceHandler 시작에 실패하였습니다.", "utf-8"));
	}
		}
		
	}else if(component == 2){
		//IRService
		if(IRService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRService가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(IRService.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRService 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRService 시작에 실패하였습니다.", "utf-8"));
	}
		}
		
	}else if(component == 3){
		//DBHandler
		if(DBService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("DBHandler가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(DBService.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("DBHandler 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("DBHandler 시작에 실패하였습니다.", "utf-8"));
	}
		}
		
	}else if(component == 4){
		//JobController
		if(JobService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("JobController가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(JobService.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("JobController 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("JobController 시작에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 5){
		//KeywordService
		if(KeywordService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("KeywordService가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(KeywordService.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("KeywordService 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("KeywordService 시작에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 6){
		//QueryCacheService
		if(QueryCacheService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("QueryCacheService가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(QueryCacheService.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("QueryCacheService 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("QueryCacheService 시작에 실패하였습니다.", "utf-8"));
	}
		}
	/* }else if(component == 7){
		//IRClassLoader
		if(IRClassLoader.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("DynamicClassLoader가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(IRClassLoader.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("DynamicClassLoader 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("DynamicClassLoader 시작에 실패하였습니다.", "utf-8"));
	}
		} */
	}else if(component == 8){
		//ManagementInfoService
		if(ManagementInfoService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("ManagementInfoService가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(ManagementInfoService.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("ManagementInfoService 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("ManagementInfoService 시작에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 9){
		//StatisticsInfoService
		if(StatisticsInfoService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("StatisticsInfoService가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(StatisticsInfoService.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("StatisticsInfoService 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("StatisticsInfoService 시작에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 10){
		//StatisticsInfoService
		if(NodeService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("NodeService가 이미 시작되었습니다.", "utf-8"));
		}else{
	if(NodeService.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("NodeService 시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("NodeService 시작에 실패하였습니다.", "utf-8"));
	}
		}
	}
	
		}else if(cmd2 == 1){
	//정지
	if(component == 0){
		//CatServer
		if(!CatServer.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("CatServer가 이미 정지되었습니다.", "utf-8"));
		}else{
	if(CatServer.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("CatServer 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("CatServer 정지에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 1){
		//ServiceHandler
		if(!WebService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("ServiceHandler가 이미 정지되었습니다.", "utf-8"));
		}else{
	if(WebService.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("ServiceHandler 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("ServiceHandler 정지에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 2){
		//IRService
		if(!IRService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRService가 이미 정지되었습니다.", "utf-8"));
		}else{
	if(IRService.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRService 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRService 정지에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 3){
		//DBHandler
		if(!DBService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("DBHandler가 이미 정지되었습니다.", "utf-8"));
		}else{
	if(DBService.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("DBHandler 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("DBHandler 정지에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 4){
		//JobController
		if(!JobService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("JobController가 이미 정지되었습니다.", "utf-8"));
		}else{
	if(JobService.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("JobController 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("JobController 정지에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 5){
		//KeywordService
		if(!KeywordService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("KeywordService가 이미 정지되었습니다.", "utf-8"));
		}else{
	if(KeywordService.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("KeywordService 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("KeywordService 정지에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 6){
		//QueryCacheService
		if(!QueryCacheService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("QueryCacheService가 이미 정지되었습니다.", "utf-8"));
		}else{
	if(QueryCacheService.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("QueryCacheService 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("QueryCacheService 정지에 실패하였습니다.", "utf-8"));
	}
		}
	/* }else if(component == 7){
		//IRClassLoader
		if(!IRClassLoader.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRClassLoader가 이미 정지되었습니다.", "utf-8"));
		}else{
	if(IRClassLoader.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRClassLoader 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRClassLoader 정지에 실패하였습니다.", "utf-8"));
	}
		} */
	}else if(component == 8){
		//ManagementInfoService
		if(!ManagementInfoService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("ManagementInfoService가 이미 정지되었습니다.", "utf-8"));
		}else{
	if(ManagementInfoService.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("ManagementInfoService 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("ManagementInfoService 정지에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 9){
		//StatisticsInfoService
		if(!StatisticsInfoService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("StatisticsInfoService가 이미 정지되었습니다.", "utf-8"));
		}else{
	if(StatisticsInfoService.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("StatisticsInfoService 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("StatisticsInfoService 정지에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 10){
		//StatisticsInfoService
		if(!NodeService.getInstance().isRunning()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("NodeService 이미 정지되었습니다.", "utf-8"));
		}else{
	if(NodeService.getInstance().stop()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("NodeService 정지에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("NodeService 정지에 실패하였습니다.", "utf-8"));
	}
		}
	}
	
		}else if(cmd2 == 2){
	//재시작
	if(component == 0){
		//CatServer
		if(CatServer.getInstance().isRunning()){
	if(CatServer.getInstance().stop() && CatServer.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("CatServer 재시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("CatServer 재시작에 실패하였습니다.", "utf-8"));
	}
		}else{
	if(CatServer.getInstance().start()){
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("CatServer 재시작에 성공하였습니다.", "utf-8"));
	}else{
		response.sendRedirect("main.jsp?message="+URLEncoder.encode("CatServer 재시작에 실패하였습니다.", "utf-8"));
	}
		}
	}else if(component == 1){
		//ServiceHandler
		if(WebService.getInstance().restart()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("ServiceHandler 재시작에 성공하였습니다.", "utf-8"));
		}else{
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("ServiceHandler 재시작에 실패하였습니다.", "utf-8"));
		}
	}else if(component == 2){
		//IRService
		if(IRService.getInstance().restart()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRService 재시작에 성공하였습니다.", "utf-8"));
		}else{
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRService 재시작에 실패하였습니다.", "utf-8"));
		}
	}else if(component == 3){
		//DBHandler
		if(DBService.getInstance().restart()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("DBHandler 재시작에 성공하였습니다.", "utf-8"));
		}else{
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("DBHandler 재시작에 실패하였습니다.", "utf-8"));
		}
	}else if(component == 4){
		//JobController
		if(JobService.getInstance().restart()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("JobController 재시작에 성공하였습니다.", "utf-8"));
		}else{
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("JobController 재시작에 실패하였습니다.", "utf-8"));
		}
	}else if(component == 5){
		//KeywordService
		if(KeywordService.getInstance().restart()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("KeywordService 재시작에 성공하였습니다.", "utf-8"));
		}else{
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("KeywordService 재시작에 실패하였습니다.", "utf-8"));
		}
	}else if(component == 6){
		//QueryCacheService
		if(QueryCacheService.getInstance().restart()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("QueryCacheService 재시작에 성공하였습니다.", "utf-8"));
		}else{
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("QueryCacheService 재시작에 실패하였습니다.", "utf-8"));
		}
	}else if(component == 7){
	/*  	//IRClassLoader
		if(IRClassLoader.getInstance().restart()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRClassLoader 재시작에 성공하였습니다.", "utf-8"));
		}else{
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("IRClassLoader 재시작에 실패하였습니다.", "utf-8"));
		} */
		
	}else if(component == 8){
		//ManagementInfoService
		if(ManagementInfoService.getInstance().restart()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("ManagementInfoService 재시작에 성공하였습니다.", "utf-8"));
		}else{
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("ManagementInfoService 재시작에 실패하였습니다.", "utf-8"));
		}
	}else if(component == 9){
		//StatisticsInfoService
		if(StatisticsInfoService.getInstance().restart()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("StatisticsInfoService 재시작에 성공하였습니다.", "utf-8"));
		}else{
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("StatisticsInfoService 재시작에 실패하였습니다.", "utf-8"));
		}
	}else if(component == 10){
		//StatisticsInfoService
		if(NodeService.getInstance().restart()){
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("NodeService 재시작에 성공하였습니다.", "utf-8"));
		}else{
	response.sendRedirect("main.jsp?message="+URLEncoder.encode("NodeService 재시작에 실패하였습니다.", "utf-8"));
		}
	}
	
		}
		
		break;
	}
	case 4:
	{
		String key = request.getParameter("key");
		boolean licenseAvail = false;
		if(LicenseSettings.getInstance().isValid(key)){
	licenseAvail = true;
	out.print("license ok");
		}else{
	out.print("license not correct");
		}
		break;
	}
	case 5:
	{
		String licenseKey = request.getParameter("license.key");
		LicenseSettings.getInstance().store(licenseKey);
		LicenseSettings.getInstance().load();
		response.sendRedirect("license.jsp?message="+URLEncoder.encode("라이선스 키를 입력하였습니다.", "utf-8"));
		break;
	}
}
%>
