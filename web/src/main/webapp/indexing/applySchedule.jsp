<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page language="java"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page contentType="text/html; charset=UTF-8"%> 
<%@page import="org.fastcatsearch.control.*"%>
<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.config.Schema"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.db.dao.IndexingSchedule"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="java.sql.Timestamp"%>

<%
	IRConfig irConfig = IRSettings.getConfig(true);
	String collectinListStr = irConfig.getString("collection.list");
	String[] colletionList = collectinListStr.split(",");
	DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
	
	String cmd = WebUtils.getString(request.getParameter("cmd"), "");	
	
	if(cmd.equals("1"))
		{
		//apply indexing schedule
		
		String slcCollection = request.getParameter("slc_collection");
		String type = request.getParameter("type");
		
		int p_day = WebUtils.getInt(request.getParameter("p_day"), -1);
		int p_hour = WebUtils.getInt(request.getParameter("p_hour"), -1);
		int p_minute = WebUtils.getInt(request.getParameter("p_minute"), -1);
		
		String s_date = request.getParameter("s_date");
		int s_hour =  WebUtils.getInt(request.getParameter("s_hour"), -1);
		int s_minute =  WebUtils.getInt(request.getParameter("s_minute"), -1);
		
		boolean isActive = false;		
		int period = p_day * 60 * 60 * 24 + p_hour * 60 * 60 + p_minute * 60;
		
		if(s_date != null && p_day>= 0 && p_hour >= 0 && s_minute >= 0 && s_hour >= 0 && s_minute >=0)
	{
	String tmp  = s_date + " " + ((s_hour < 9) ? "0" + s_hour : s_hour) 
	+ ":" + ((s_minute < 9) ? "0" + s_minute : s_minute)  + ":00";
	
	//out.println(type+","+p_day+","+tmp);
	
	try{
		Timestamp startTime = Timestamp.valueOf(tmp);
		int rsCount = dbHandler.IndexingSchedule.updateOrInsert(slcCollection, type, period, startTime, isActive);
		if ( rsCount > 0 )
	out.print(0);
		else
	out.print(1);
		}
	catch(Exception e){
		e.printStackTrace();
		}			
	}
		//stop scheduling
		JobService.getInstance().toggleIndexingSchedule(slcCollection, type, isActive);		
		
		}
	else if(cmd.equals("2"))
		{
		//toggle indexing schedule
		
		String slcCollection = request.getParameter("slc_collection");
		String type = request.getParameter("type");
		String isActiveStr =  WebUtils.getString(request.getParameter("isActive"), "0");
		
		boolean isActive = false;
		if(isActiveStr.equals("1"))
	isActive = true;		
		
		try
	{
	dbHandler.IndexingSchedule.updateStatus(slcCollection, type, isActive);
	}
	catch(Exception e)
	{
	e.printStackTrace();
	}
		
		//JobScheduler RELOAD!
		boolean isSuccess = JobService.getInstance().toggleIndexingSchedule(slcCollection, type, isActive);		
		
		IndexingSchedule schedule = dbHandler.IndexingSchedule.select(slcCollection, type);
		if ( schedule != null && schedule.isActive )		
	out.print(0);
		else
	out.print(1);	
		}
%>

