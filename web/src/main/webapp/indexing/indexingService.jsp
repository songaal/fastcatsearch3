<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="org.fastcatsearch.job.AddIndexingJob"%>
<%@page import="org.fastcatsearch.job.FullIndexingJob"%>
<%@page import="org.fastcatsearch.job.cluster.*"%>
<%@page import="org.fastcatsearch.control.JobService"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.fastcatsearch.ir.common.*"%>


<%@include file="../common.jsp" %>

<%
	int cmd = Integer.parseInt(request.getParameter("cmd"));
String collection = request.getParameter("collection");
JobService jobService = ServiceManager.getInstance().getService(JobService.class);
switch(cmd){
	//start full indexing 
	case 0:
	{
		FullIndexingJob job = new FullIndexingJob();
		job.setArgs(new String[]{collection});
		jobService.offer(job);
		//response.sendRedirect("result.jsp?message="+URLEncoder.encode(URLEncoder.encode("컬렉션 "+collection+"의 전체색인 작업을 등록하였습니다.", "utf-8"),"utf-8"));
		break;
	}
	//start incremental indexing 
	case 1:
	{
		AddIndexingJob job = new AddIndexingJob();
		job.setArgs(new String[]{collection});
		jobService.offer(job);
		//response.sendRedirect("result.jsp?message="+URLEncoder.encode(URLEncoder.encode("컬렉션 "+collection+"의 증분색인 작업을 등록하였습니다.", "utf-8"),"utf-8"));
		break;
	}
	case 2://분산전체색인
	{
		ClusterIndexingJob job = new ClusterIndexingJob(IndexingType.FULL);
		job.setArgs(new String[]{collection});
		jobService.offer(job);
		//response.sendRedirect("result.jsp?message="+URLEncoder.encode(URLEncoder.encode("컬렉션 "+collection+"의 전체색인 작업을 등록하였습니다.", "utf-8"),"utf-8"));
		break;
	}
	case 3://분산증분색인
	{
		ClusterIndexingJob job = new ClusterIndexingJob(IndexingType.ADD);
		job.setArgs(new String[]{collection});
		jobService.offer(job);
		//response.sendRedirect("result.jsp?message="+URLEncoder.encode(URLEncoder.encode("컬렉션 "+collection+"의 전체색인 작업을 등록하였습니다.", "utf-8"),"utf-8"));
		break;
	}
}
%>
