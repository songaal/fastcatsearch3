<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="org.fastcatsearch.control.JobService"%>
<%@page import="org.fastcatsearch.control.JobScheduler"%>
<%@ page contentType="text/html; charset=UTF-8"%>

<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="org.fastcatsearch.web.WebUtils"%>
<%@page import="org.fastcatsearch.ir.settings.Schema"%>
<%@page import="org.fastcatsearch.ir.config.DataSourceConfig"%>
<%@page import="java.io.File"%>
<%@page import="java.io.FileOutputStream"%>
<%@page import="java.util.Properties"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.io.IOException"%>
<%@page import="org.fastcatsearch.ir.search.CollectionHandler"%>
<%@page import="org.fastcatsearch.ir.IRService"%>
<%@page import="org.fastcatsearch.service.*"%>

<%@page import="org.apache.commons.io.FileUtils"%>
<%@page import="org.json.*"%>
<%@page import="java.sql.*"%>
<%@page import="java.text.*"%>
<%@page import="org.fastcatsearch.db.*"%>
<%@page import="org.fastcatsearch.db.dao.*"%>

<%@include file="../common.jsp"%>

<%
	int cmd = Integer.parseInt(request.getParameter("cmd"));
	IRService irService = ServiceManager.getInstance().getService(IRService.class);
	
	switch (cmd) {
	//stop collection service
	case 0: {
		String collection = "";
		try {
			collection = request.getParameter("collection");
			CollectionHandler handler = irService.removeCollectionHandler(collection);
			handler.close();
		} catch (IOException e) {
			response.sendRedirect("main.jsp?message="
			+ URLEncoder.encode(URLEncoder.encode("컬렉션 " + collection + "을 중지하지 못했습니다.", "utf-8"), "utf-8"));
		} finally {
			response.sendRedirect("main.jsp");
		}
		break;
	}
	//start collection service
	case 1: {
		String collection = "";
		try {
			collection = request.getParameter("collection");
			File collectionDir = null;//IRSettings.getCollectionHomeFile(collection);
			Schema schema = null;//IRSettings.getSchema(collection, true);
		
			CollectionHandler oldHandler = null;//irService.putCollectionHandler(collection,
			//new CollectionHandler(collection, collectionDir, schema, null));
			try {
				if (oldHandler != null)
			oldHandler.close();
			} catch (IOException e) {
			}
		} finally {
			response.sendRedirect("main.jsp");
		}
		break;
	}
	//ADD collection
	case 2: {
		String collection = "";
		collection = request.getParameter("collection");
		
		CollectionHandler collectionHandler = irService.createCollection(collection);
		if(collectionHandler != null){
			response.sendRedirect("main.jsp?message="
			+ URLEncoder.encode(URLEncoder.encode(collection + " 컬렉션을 생성하였습니다.", "utf-8"), "utf-8"));
		} else {
			response.sendRedirect("main.jsp?message="
			+ URLEncoder.encode(URLEncoder.encode("설정파일에 " + collection + " 컬렉션이 존재합니다.", "utf-8"), "utf-8"));
		}

		break;
	}
	//REMOVE collection
	case 3: {
		String collection = "";
		collection = request.getParameter("collection");
		CollectionHandler handler = irService.collectionHandler(collection);
		if (handler != null) {
	response.sendRedirect("main.jsp?message="
	+ URLEncoder.encode(URLEncoder.encode(collection + " 컬렉션이 실행중이므로 삭제할수 없습니다.", "utf-8"), "utf-8"));
	return;
		} else {
	/*
	IRService에서 Collection을 삭제한다.
	 */
	collection = request.getParameter("collection");
	handler = irService.removeCollectionHandler(collection);
	if (handler != null)
		handler.close();

	/**
	DBHandler에서 IndexingSchedule에서 Collection 삭제한다.
	DBHandler에서 IndexingResult에서 Collection 삭제한다.
	 */
	DBService dbService = DBService.getInstance();
	IndexingSchedule indexingSchedule = dbService.getDAO("IndexingSchedule", IndexingSchedule.class);
	indexingSchedule.delete(collection);
	
	IndexingResult indexingResult = dbService.getDAO("IndexingResult", IndexingResult.class);
	indexingResult.delete(collection, IndexingResult.TYPE_FULL_INDEXING);
	indexingResult.delete(collection, IndexingResult.TYPE_INC_INDEXING);
	
	/**
	JobController에서 스켸쥴링된 작업을 다시 읽어 온다. 
	 */
	JobService.getInstance().reloadSchedules();

	final String COLLECTION_LIST_KEY = "collection.list";
	//IRConfig irconfig = IRSettings.getConfig();
	Properties props = null;//irconfig.getProperties();
	String collectionList = props.getProperty(COLLECTION_LIST_KEY);
	String[] list = collectionList.split(",");
	String listString = "";

	for (int i = 0; i < list.length; i++) {
		String s = list[i];
		if (!(collection.equalsIgnoreCase(s))) {
	listString += s;
	listString += ",";
		}
	}

	if (listString.length() > 0) {
		listString = listString.substring(0, listString.length() - 1);
	}
	props.put(COLLECTION_LIST_KEY, listString);
	//IRSettings.storeConfig(irconfig);
	//컬렉션 파일들 삭제.
	String collectionHome = null;//IRSettings.getCollectionHome(collection);
	File collectionDirectory = new File(collectionHome);
	if (collectionDirectory != null && collectionDirectory.exists()) {
		FileUtils.deleteDirectory(collectionDirectory);
	}

	response.sendRedirect("main.jsp?message="
	+ URLEncoder.encode(URLEncoder.encode(collection + " 컬렉션을 삭제하였습니다.", "utf-8"), "utf-8"));
		}
		break;
	}
	//ADD field
	case 4: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");

		int tag = 0;//IRSettings.addField2WorkSchema(collection);
		//Schema schema = IRSettings.getWorkSchema(collection);

		//FieldSetting fieldSetting = new FieldSetting("newfield", "int", -1);
		//schema.addFieldSetting(fieldSetting);
		out.print(tag);
		break;
	}
	//REMOVE field
	case 5: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		String fieldName = request.getParameter("fieldname");
		int tag = 0;//.deleteField2WorkSchema(collection, fieldName);
		out.print(tag);
		break;
	}

	//save datasource
	case 6: {
		
		String collection = "";
		String sourceType = "";
		//.storeDataSourceSetting(collection, setting);
		response.sendRedirect("datasource.jsp?collection=" + collection + "&sourceType=" + sourceType + "&message="
		+ URLEncoder.encode(URLEncoder.encode("설정을 저장하였습니다.", "utf-8"), "utf-8"));

		break;
	}
	//update workSchema
	case 7: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		String key = request.getParameter("key");
		String value = request.getParameter("value");
		int tag = 0;//.updateWorkSchema(collection, key, value);
		out.print(tag);
		break;
	}

	//recover workSchema
	case 8: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		String fieldname = request.getParameter("fieldname");
		int tag = 0;//.recoverWorkeSchema(collection, fieldname);
		if (tag == 0) {
	response.sendRedirect("schemaEditer.jsp?message="
	+ URLEncoder.encode(URLEncoder.encode(fieldname + "필드를 복원하였습니다.", "utf-8"), "utf-8"));
		} else {
	response.sendRedirect("schemaEditer.jsp?message="
	+ URLEncoder.encode(URLEncoder.encode(fieldname + "필드를 복원이 실패하였습니다.", "utf-8"), "utf-8"));
		}
		break;
	}

	//isChange
	case 9: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		String key = request.getParameter("key");
		int tag = 0;//IRSettings.isChange(collection, key);
		// tag = 1 고쳐졌음
		out.print(tag);
		break;
	}
	//deleteWorkSchema
	case 10: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		int tag = 0;//IRSettings.deleteWorkSchema(collection);
		out.print(tag);
		break;
	}
	//필수사항. pk와 색인필드 여부 확인.
	case 11: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		int ret = 0;//IRSettings.checkWorkSchema(collection);
		out.print(ret);
		break;
	}
	//get
	/* case 12: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		String category = request.getParameter("cate");
		int rows = Integer.parseInt(request.getParameter("rows") == null ? "10" : request.getParameter("rows"));
		int pageNum = Integer.parseInt(request.getParameter("page") == null ? "1" : request.getParameter("page"));

		DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
		DBContext dbContext = dbHandler.getDBContext();
		PreparedStatement pstmt = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
	String countSQL = "SELECT count(*) FROM " + collection + "WebPageSource where cate = '" + category + "'";
	stmt = conn.createStatement();
	rs = stmt.executeQuery(countSQL);
	int totalCount = 0;
	if (rs.next()) {
		totalCount = rs.getInt(1);
	}
	rs.close();
	stmt.close();

	String selectSQL = null;
	selectSQL = "SELECT * FROM ( SELECT ROW_NUMBER() OVER() AS rownum, " + collection + "WebPageSource.* FROM "
	+ collection + "WebPageSource ) AS tmp WHERE cate = ? and rownum >= ? and rownum <= ?";
	pstmt = conn.prepareStatement(selectSQL);
	int parameterIndex = 1;
	pstmt.setString(parameterIndex++, category);
	pstmt.setInt(parameterIndex++, (pageNum - 1) * rows + 1);
	pstmt.setInt(parameterIndex++, pageNum * rows);
	rs = pstmt.executeQuery();
	JSONStringer stringer = new JSONStringer();
	stringer.object();
	stringer.key("total").value("" + totalCount);
	stringer.key("rows").array();
	while (rs.next()) {
		parameterIndex = 2;
		int id = rs.getInt(parameterIndex++);
		String link = rs.getString(parameterIndex++);
		String title = rs.getString(parameterIndex++);
		String cate = rs.getString(parameterIndex++);
		String encoding = rs.getString(parameterIndex++);
		Timestamp upt_ts = rs.getTimestamp(parameterIndex++);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String upt_dt = sdf.format(new Date(upt_ts.getTime()));
		stringer.object().key("id").value(id).key("link").value(link).key("title").value(title).key("cate")
		.value(cate).key("encoding").value(encoding).key("upt_dt").value(upt_dt).endObject();
	}
	stringer.endArray();
	stringer.endObject();
	out.print(stringer.toString());
		} catch (SQLException e) {
	out.print(-1);
	return;
		} finally {
	try {
		if (rs != null)
	rs.close();
		if (pstmt != null)
	pstmt.close();
	} catch (SQLException e) {
	}
		}

		break;
	}
	//insert
	case 13: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		String id = request.getParameter("id");
		String link = request.getParameter("link");
		String title = request.getParameter("title");
		String cate = request.getParameter("cate");
		String upt_dt = request.getParameter("upt_dt");

		DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
		Connection conn = dbHandler.getConn();
		PreparedStatement pstmt = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
	String insertSQL = "insert into "
	+ collection
	+ "WebPageSource(id, link, title, cate, upt_dt) (select case when max(id) is null then 0 else max(id)+1 end,?,?,?,? from "
	+ collection + "WebPageSource)";
	pstmt = conn.prepareStatement(insertSQL);
	int parameterIndex = 1;
	pstmt.setString(parameterIndex++, link);
	pstmt.setString(parameterIndex++, title);
	pstmt.setString(parameterIndex++, cate);
	pstmt.setTimestamp(parameterIndex++, new Timestamp(sdf.parse(upt_dt).getTime()));
	pstmt.executeUpdate();
		} catch (SQLException e) {
	e.printStackTrace();
		} finally {
	try {
		if (pstmt != null)
	pstmt.close();
	} catch (SQLException e) {
		e.printStackTrace();
	}
		}
		JSONStringer stringer = new JSONStringer();
		stringer.object().key("id").value(id).key("link").value(link).key("title").value(title).key("cate").value(cate)
		.key("upt_dt").value(upt_dt).endObject();
		out.print(stringer.toString());
		break;
	}
	//update
	case 14: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		String id = request.getParameter("id");
		String link = request.getParameter("link");
		String title = request.getParameter("title");
		String cate = request.getParameter("cate");
		String upt_dt = request.getParameter("upt_dt");

		DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
		Connection conn = dbHandler.getConn();
		PreparedStatement pstmt = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
	String updateSQL = "UPDATE " + collection
	+ "WebPageSource SET link = ?,title = ?,cate = ?,upt_dt = ? WHERE id = ?";
	pstmt = conn.prepareStatement(updateSQL);
	int parameterIndex = 1;
	pstmt.setString(parameterIndex++, link);
	pstmt.setString(parameterIndex++, title);
	pstmt.setString(parameterIndex++, cate);
	pstmt.setTimestamp(parameterIndex++, new Timestamp(sdf.parse(upt_dt).getTime()));
	pstmt.setString(parameterIndex++, id);
	pstmt.executeUpdate();
		} catch (SQLException e) {
	e.printStackTrace();
		} finally {
	try {
		if (pstmt != null)
	pstmt.close();
	} catch (SQLException e) {
		e.printStackTrace();
	}
		}
		JSONStringer stringer = new JSONStringer();
		stringer.object().key("id").value(id).key("link").value(link).key("title").value(title).key("cate").value(cate)
		.key("upt_dt").value(upt_dt).endObject();
		out.print(stringer.toString());
		break;
	}
	//delete
	case 15: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		String id = request.getParameter("id");

		DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
		Connection conn = dbHandler.getConn();
		PreparedStatement pstmt = null;
		try {
	String deleteSQL = "delete from " + collection + "WebPageSource WHERE id = ?";
	pstmt = conn.prepareStatement(deleteSQL);
	int parameterIndex = 1;
	pstmt.setString(parameterIndex++, id);
	pstmt.executeUpdate();
		} catch (SQLException e) {
	e.printStackTrace();
		} finally {
	try {
		if (pstmt != null)
	pstmt.close();
	} catch (SQLException e) {
		e.printStackTrace();
	}
		}
		JSONStringer stringer = new JSONStringer();
		stringer.object().key("success").value(true).endObject();
		out.print(stringer.toString());
		break;
	}
	//get category
	case 16: {
		response.setContentType("text/html");
		String collection = request.getParameter("collection");
		DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
		Connection conn = dbHandler.getConn();
		Statement stmt = null;
		ResultSet re = null;
		JSONStringer stringer = new JSONStringer();
		try {
	String selectSQL = "select DISTINCT cate from " + collection + "WebPageSource";
	stmt = conn.createStatement();
	re = stmt.executeQuery(selectSQL);
	stringer.array();
	while (re.next()) {
		String cate = re.getString("cate");
		stringer.object().key("cate").value(cate).endObject();
	}
	stringer.endArray();
		} catch (SQLException e) {
	//e.printStackTrace();
	return;
		} finally {
	try {
		if (stmt != null)
	stmt.close();
	} catch (SQLException e) {
		e.printStackTrace();
	}
		}
		out.print(stringer.toString());
		break;
	} */
	default:
		break;
	}
%>
