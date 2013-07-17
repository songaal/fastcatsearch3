<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/plain; charset=UTF-8"%> 
<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.settings.Schema"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.ir.config.DataSourceConfig"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="java.io.*"%>
<%@page import="java.sql.*"%>
<%@page import="org.fastcatsearch.db.DBService" %>
<%@page import="org.fastcatsearch.db.dao.*" %>
<%@page import="org.fastcatsearch.db.vo.*" %>
<%@page import="org.apache.commons.fileupload.FileItem"%>
<%@page import="org.apache.commons.fileupload.FileItemFactory"%>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%
	response.setContentType("text/html");
	int cmd = -1;
	InputStream in = null;
	BufferedReader br = null;
	FileItemFactory factory = new DiskFileItemFactory();
	ServletFileUpload upload = new ServletFileUpload(factory);
	List<FileItem> items = upload.parseRequest(request);
	for(FileItem fitem : items) {
		if(fitem.isFormField() && "cmd".equals(fitem.getFieldName())) {
			cmd = Integer.parseInt(fitem.getString());
		} else if(!fitem.isFormField()) {
			in = fitem.getInputStream();
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		}
	}
	
	if(br!=null) {
		
		try {
	
			String keyword = null;
			
			switch(cmd){
				case 0: //추천어
				{
					DBService dbService = DBService.getInstance();
					SetDictionary recommendKeyword = dbService.getDAO("RecommendKeyword", SetDictionary.class);										
					BatchContext batchContext = recommendKeyword.startInsertBatch();
					try
					{
						if (batchContext == null) {
							out.print(0);
							return;
						}
				
						while ((keyword = br.readLine()) != null) {
							if (-1 == recommendKeyword.insertBatch(keyword, batchContext)) {
								in.close();
								out.print(0);
								return;
							}
						}
				
						if (recommendKeyword.endInsertBatch(batchContext) == false)
							out.print(0);
						else
							out.print(1);
						
						in.close();
					}
					finally
					{
						if ( batchContext != null )
							batchContext.close();
					}
					break;
				
				}
			}
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}
	}
%>
