<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@ page contentType="text/plain; charset=UTF-8"%>
<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.config.Schema"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.datasource.DataSourceSetting"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.List"%>
<%@page import="java.io.*"%>
<%@page import="java.sql.*"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.db.dao.*"%>
<%@page import="org.fastcatsearch.db.vo.*"%>
<%@page import="org.fastcatsearch.service.*"%>
<%@page import="org.apache.commons.fileupload.FileItem"%>
<%@page import="org.apache.commons.fileupload.FileItemFactory"%>
<%@page import="org.apache.commons.fileupload.disk.DiskFileItemFactory"%>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload"%>
<%
	response.setContentType("text/html");
	int cmd = -1;
	String collection = null;
	String category = null;
	InputStream in = null;
	BufferedReader br = null;
	FileItemFactory factory = new DiskFileItemFactory();
	ServletFileUpload upload = new ServletFileUpload(factory);
	List<FileItem> items = upload.parseRequest(request);
	for (FileItem fitem : items) {
		if (fitem.isFormField() && "cmd".equals(fitem.getFieldName())) {
			cmd = Integer.parseInt(fitem.getString());
		} else if (fitem.isFormField() && "collection".equals(fitem.getFieldName())) {
			collection = fitem.getString();
		} else if (fitem.isFormField() && "category".equals(fitem.getFieldName())) {
			category = fitem.getString();
		} else if (!fitem.isFormField()) {
			in = fitem.getInputStream();
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		}
	}

	if (br != null) {

		try {

			DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
			SetDictionary synonymDictionary = dbHandler.getDAO(category+"SynonymDictionary", SetDictionary.class);
			SetDictionary stopDictionary = dbHandler.getDAO(category+"StopDictionary", SetDictionary.class);
			SetDictionary userDictionary = dbHandler.getDAO(category+"UserDictionary", SetDictionary.class);
			
			String keyword = null;
			
			switch (cmd) {
			case 0: //유사어사전
			{
				BatchContext batchContext = synonymDictionary.startInsertBatch();
				try
				{
				if (batchContext == null) {
					out.print(0);
					return;
				}
		
				while ((keyword = br.readLine()) != null) {
					if (-1 == synonymDictionary.insertBatch(keyword, batchContext)) {
						in.close();
						out.print(0);
						return;
					}
				}
		
				if (synonymDictionary.endInsertBatch(batchContext) == false)
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
			case 1: //금지어사전
			{
				BatchContext batchContext = stopDictionary.startInsertBatch();
				try
				{
				if (batchContext == null) {
					out.print(0);
					return;
				}
		
				while ((keyword = br.readLine()) != null) {
					if (-1 == stopDictionary.insertBatch(keyword, batchContext)) {
						in.close();
						out.print(0);
						return;
					}
				}
		
				if (stopDictionary.endInsertBatch(batchContext) == false)
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
			case 2: //사용자사전
			{
		
				BatchContext batchContext = userDictionary.startInsertBatch();
				try {
					if (batchContext == null) {
						out.print(0);
						return;
					}
		
					while ((keyword = br.readLine()) != null) {
						if (-1 == userDictionary.insertBatch(keyword, batchContext)) {
							in.close();
							out.print(0);
							return;
						}
					}
		
					if (userDictionary.endInsertBatch(batchContext) == false)
						out.print(0);
					else
						out.print(1);
					in.close();
				} finally {
					if (batchContext != null)
						batchContext.close();
				}
				break;
			}
			/* case 3: //웹페이지 데이터소스 파일
			{
				try {
					DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
					Connection conn = dbHandler.getConn();
					conn.setAutoCommit(false);
					PreparedStatement pstmt = null;
					ResultSet rs = null;
					Statement stmt = null;
		
					try {
						String dropSQL = "drop table " + collection + "WebPageSource";
						stmt = conn.createStatement();
						stmt.executeUpdate(dropSQL);
					} catch (SQLException e) {
						//FIXME:업로드시 데이터 초기화됨.
					} finally {
						try {
							if (stmt != null)
								stmt.close();
						} catch (SQLException e) {
						}
					}
		
					//create table
					try {
						String createSQL = "create table "
						                + collection
						                + "WebPageSource (id int primary key,link varchar(255) not null, title varchar(255),cate varchar(255), encoding varchar(255), upt_dt timestamp)";
						stmt = conn.createStatement();
						stmt.executeUpdate(createSQL);
					} catch (SQLException e) {
						e.printStackTrace();
						out.print(0);
						return;
					} finally {
						try {
							if (stmt != null)
								stmt.close();
						} catch (SQLException e) {
							out.print(0);
							return;
						}
					}
		
					int inx = 0;
		
					pstmt = conn.prepareStatement("insert into " + collection + "WebPageSource (id,link,title,encoding,cate,upt_dt) values (?,?,?,?,?,?)");
		
					int i = 0;
					int lineNum = 0;
					String cate = "none";
					while ((temp = br.readLine()) != null) {
						lineNum++;
		
						if (temp.contains("[") && !temp.contains("http")) {
							int pos_0 = temp.indexOf("[") + 1;
							int pos_1 = temp.indexOf("]");
							cate = temp.substring(pos_0, pos_1);
							continue;
						}
						if ("".equals(temp.trim()) || !temp.contains("http")) {
							continue;
						}
		
						String[] tmps = temp.split("\\t");
						if (tmps.length >= 1) {
							try {
								int parameterIndex = 1;
								pstmt.setInt(parameterIndex++, inx++);
								pstmt.setString(parameterIndex++, tmps[0]);
								if (tmps.length == 2) {
									pstmt.setString(parameterIndex++, tmps[1]);
									pstmt.setString(parameterIndex++, "utf-8");
								} else if (tmps.length == 3) {
									pstmt.setString(parameterIndex++, tmps[1]);
									pstmt.setString(parameterIndex++, tmps[2]);
								} else {
									pstmt.setString(parameterIndex++, "");
									pstmt.setString(parameterIndex++, "utf-8");
								}
								pstmt.setString(parameterIndex++, cate);
								pstmt.setTimestamp(parameterIndex++, new Timestamp(new Date().getTime()));
								pstmt.addBatch();
							} catch (SQLException e) {
								e.printStackTrace();
								out.print(0);
								return;
							}
							i++;
							if ((i % 1000) == 0) {
								pstmt.executeBatch();
								conn.commit();
							}
						} else {
							return;
						}
					}
					pstmt.executeBatch();
					pstmt.close();
					conn.commit();
					conn.setAutoCommit(true);
					in.close();
					out.print(1);
				} catch (Exception e) {
					out.print(0);
				}
				break;
			} */
			default: {
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
