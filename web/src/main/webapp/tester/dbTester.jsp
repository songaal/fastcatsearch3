<%--
# Copyright (C) 2011 - 2013 Websquared, Inc.
# All rights reserved.
--%>

<%@page import="java.lang.reflect.Field"%>
<%@page import="com.fastcatsearch.util.WebUtils"%>
<%@page import="org.fastcatsearch.settings.IRSettings"%>
<%@page import="org.fastcatsearch.ir.analysis.FieldTokenizer"%>
<%@page import="org.fastcatsearch.ir.io.CharVector"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="org.fastcatsearch.db.DBContext"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.sql.Types"%>
<%@page import="java.io.StringWriter"%>
<%@page import="org.fastcatsearch.ir.util.Formatter"%>
<%@page import="org.fastcatsearch.db.DBService"%>
<%@page import="java.sql.ResultSetMetaData"%>
<%@page import="org.fastcatsearch.ir.config.IRConfig"%>
<%@page import="org.fastcatsearch.util.DynamicClassLoader2"%>
<%@page import="org.fastcatsearch.service.*"%>

<%@page contentType="text/html; charset=UTF-8"%> 

<%@include file="../common.jsp" %>

<%
	String dbType = WebUtils.getString(request.getParameter("dbType"),"");
	String dbName = WebUtils.getString(request.getParameter("dbname"),"");
	String collection = WebUtils.getString(request.getParameter("collection"),"");
	
	String userSQL = WebUtils.getString(request.getParameter("userSQL"),"").trim();
	String executeType = WebUtils.getString(request.getParameter("executeType"),"U");
	String message = "";
	String resultString = "";
	ResultSet rs = null;
	int n = 0;
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@page import="org.fastcatsearch.datasource.DataSourceSetting"%>
<%@page import="org.fastcatsearch.ir.config.Schema"%>
<%@page import="java.sql.Driver"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.util.Properties"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.Statement"%><html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>FASTCAT 검색엔진 관리도구</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
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
	<script type="text/javascript">
		
		function updateQuery(){
			if($("#userSQL").val() == ''){
				alert("SQL문이 없습니다.");
				return;
			}
			
			if($("#dbType").val() == 'datasource'){
				$("#executeType").val('U_D');
			}else{
				if($("#dbname").val() == 'db'){
					$("#executeType").val('U_DB');
				}else{
					$("#executeType").val('U_MONDB');
				}
			}
			
			$("#analyzerForm").submit();
		}
		function selectQuery(){
			if($("#userSQL").val() == ''){
				alert("SQL문이 없습니다.");
				return;
			}
			
			if($("#dbType").val() == 'datasource'){
				$("#executeType").val('S_D');
			}else{
				if($("#dbname").val() == 'db'){
					$("#executeType").val('S_DB');
				}else{
					$("#executeType").val('S_MONDB');
				}
			}
			//alert($("#dbType").val() +", "+$("#dbname").val()+", "+$("#executeType").val());
			$("#analyzerForm").submit();
		}

		function selectOption(){
			$("#executeType").val('N');
			$("#analyzerForm").submit();
			return true;
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
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/dbTester.jsp" class="selected">DB테스트</a></li>
			<li><a href="<%=FASTCAT_MANAGE_ROOT%>tester/searchDoc.jsp">문서원문조회</a></li>
			</ul>
	</div>
</div><!-- E : #sidebar -->


<div id="mainContent">

<h2>DB테스트</h2>
<form action="dbTester.jsp" method="post" id="analyzerForm">
<input type="hidden" name="executeType" id="executeType"/>
<div class="fbox">
<table class="tbl01">
<colgroup><col width="15%" /><col width="" /></colgroup>
<tbody>
	<tr>
		<th>DB 선택</th>
		<td style="text-align:left">
		<select name="dbType" id="dbType" onchange="javascript:selectOption()">
		<option value="" >::선택::</option>
		<option value="fastcat" <%=dbType.equals("fastcat") ? "selected" : ""%> >패스트캣 내부 DB</option>
		<option value=datasource <%=dbType.equals("datasource") ? "selected" : ""%> >데이터소스</option>
		</select>
		<%
			if(dbType.equals("datasource")){
				/* IRConfig irConfig = IRSettings.getConfig();
				String collectinListStr = irConfig.getString("collection.list");
				String[] colletionList = collectinListStr.split(","); */
				
				IRService irService = ServiceManager.getInstance().getService(IRService.class);
				List<Collection> collectionList = irService.getCollectionList();
		%>
		<select name="collection" id="collection" onchange="javascript:selectOption()">
		<%
			for(Collection col = collectionList){
		%>
		<option value="<%=col.getId()%>" <%=col.getId().equals(collection) ? "selected" : ""%> ><%=col.getName()%>(<%=col.getId()%>)</option>
		<%
			}
		%>
		</select>
		<%
			}
				if(dbType.equals("fastcat")){
		%>
		<select name="dbname" id="dbname" onchange="javascript:selectOption()">
		<option value="db" <%="db".equals(dbName) ? "selected" : ""%> >db</option>
		<option value="mondb" <%="mondb".equals(dbName) ? "selected" : ""%> >mondb</option>
		</select>
		<%
			}		
				
				if(dbType.equals("datasource")){
		%>
			<i>※외부데이터 조회테스트는 100건으로 제한됩니다.</i> 
			<%
 				}
 			%>
		</td>
	</tr>
	<tr>
		<th>실행할 SQL</th>
		<td style="text-align:left"><textarea name="userSQL" cols="99" rows="10" id="userSQL"><%=userSQL%></textarea></td>
	</tr>
	<tr>
		<td colspan="2"><a href="javascript:selectQuery()" class="btn">SELECT 실행</a>
		<a href="javascript:updateQuery()" class="btn">업데이트</a></td>
	</tr>
	<%
		//Connection extConn = null;
		
		DBService dbHandler = ServiceManager.getInstance().getService(DBService.class);
		DBContext dbContext = dbHandler.getDBContext();
		if(userSQL.length() > 0){
			if(executeType.equalsIgnoreCase("U_DB")){
		try{
			long st = System.currentTimeMillis();
			n = dbContext.updateSQL(userSQL);
			resultString = n+" 개의 행이 적용되었습니다. "+Formatter.getFormatTime(System.currentTimeMillis() - st);
		}catch(SQLException e){
			message = e.getMessage();
		}
			}else if(executeType.equalsIgnoreCase("U_MONDB")){
		try{
			long st = System.currentTimeMillis();
			n = dbContext.updateSQL(userSQL);
			resultString = n+" 개의 행이 적용되었습니다. "+Formatter.getFormatTime(System.currentTimeMillis() - st);
		}catch(SQLException e){
			message = e.getMessage();
		}
			}else if(executeType.equalsIgnoreCase("S_DB")){
		try{
			long st = System.currentTimeMillis();
			rs = dbContext.selectSQL(userSQL);
			resultString = "성공적으로 실행되었습니다. "+Formatter.getFormatTime(System.currentTimeMillis() - st);
		}catch(SQLException e){
			message = e.getMessage();
			dbContext.close();
		}
			}else if(executeType.equalsIgnoreCase("S_MONDB")){
		try{
			long st = System.currentTimeMillis();
			rs = dbContext.selectSQL(userSQL);
			resultString = "성공적으로 실행되었습니다. "+Formatter.getFormatTime(System.currentTimeMillis() - st);
		}catch(SQLException e){
			message = e.getMessage();
			dbContext.close();
		}
			}else if(executeType.equalsIgnoreCase("U_D")){
		try{
			long st = System.currentTimeMillis();
			Schema schema = IRSettings.getSchema(collection, false);
			DataSourceSetting dsSetting = IRSettings.getDatasource(collection, false);
			
			if(dsSetting.driver != null){
				Object object = DynamicClassLoader.loadObject(dsSetting.driver);
				if(object == null){
					message = "Cannot find sql driver = "+dsSetting.driver;
				}else{
					Driver driver = (Driver)object;
					DriverManager.registerDriver(driver);
					Properties info = new Properties();
					info.put("user", dsSetting.user);
					info.put("password", dsSetting.password);
					Connection con = null;
					try{
						con = driver.connect(dsSetting.url, info);
						if(con != null){
							con.setAutoCommit(true);
							Statement stmt = con.createStatement();
							n = stmt.executeUpdate(userSQL);
							stmt.close();
							resultString = n+" 개의 행이 적용되었습니다. "+Formatter.getFormatTime(System.currentTimeMillis() - st);
						}else{
							message = "서버에 연결에 실패하였습니다. JDBC URL 또는 사용자 계정정보가 올바른지 확인하여 주십시오.<br/>";
							message += "JDBC_URL = "+dsSetting.url+", USER = "+dsSetting.user+", PASSWD = "+dsSetting.password;
						}
					}finally{
						con.close();
					}
				}
			}
		}catch(SQLException e){
			message = e.getMessage();
		}
		
			}else if(executeType.equalsIgnoreCase("S_D")){
		
		try{
			long st = System.currentTimeMillis();
			Schema schema = IRSettings.getSchema(collection, false);
			DataSourceSetting dsSetting = IRSettings.getDatasource(collection, false);
			if(dsSetting.driver != null){
				Object object = DynamicClassLoader.loadObject(dsSetting.driver);
			
				if(object == null){
					message = "Cannot find sql driver = "+dsSetting.driver;
				}else{
					Driver driver = (Driver)object;
					DriverManager.registerDriver(driver);
					Properties info = new Properties();
					info.put("user", dsSetting.user);
					info.put("password", dsSetting.password);
					Connection extConn = driver.connect(dsSetting.url, info);
					if(extConn != null){
						extConn.setAutoCommit(true);
						Statement stmt = extConn.createStatement();
						stmt.setMaxRows(100);
						rs = stmt.executeQuery(userSQL);
						resultString = "성공적으로 실행되었습니다. "+Formatter.getFormatTime(System.currentTimeMillis() - st);
					}else{
						message = "서버에 연결에 실패하였습니다. JDBC URL 또는 사용자 계정정보가 올바른지 확인하여 주십시오.<br/>";
						message += "JDBC_URL = "+dsSetting.url+", USER = "+dsSetting.user+", PASSWD = "+dsSetting.password;
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
			message = e.getMessage();
		}
			}
	%>
		<tr>
		<th>메시지</th>
		<td style='text-align:left'>
			<%=message %>
		</td>
		</tr>
		<tr>
		<th>실행결과</th>
		<td style='text-align:left'><%=resultString%></td>
		</tr>
		<%
	}
	%>
</tbody>
</table>
</div>

<%
if(executeType.equalsIgnoreCase("S") || executeType.equalsIgnoreCase("S_D") || executeType.equalsIgnoreCase("S_DB") || executeType.equalsIgnoreCase("S_MONDB") ){
%>
	<table id="sqlResult"></table>
<%
}
%>
</form>
	
<!-- E : #mainContent --></div>
	
<!-- footer -->
<%@include file="../footer.jsp" %>
	
</div><!-- //E : #container -->

</body>

</html>
<%
if(executeType.equalsIgnoreCase("S") || executeType.equalsIgnoreCase("S_D") || executeType.equalsIgnoreCase("S_DB") || executeType.equalsIgnoreCase("S_MONDB")){
	if(rs != null){
		ResultSetMetaData meta = rs.getMetaData();
		int cc = meta.getColumnCount();
		
		StringBuffer sb = new StringBuffer(); 
		StringBuffer sb2 = new StringBuffer(); 
		for(int i=1; i<=cc; i++){
			//colNames
			sb.append("'");
			sb.append(meta.getColumnName(i));
			sb.append("'");
			if(i < cc)
				sb.append(",");
			
			//colModel
			sb2.append("{name:'");
			sb2.append(meta.getColumnName(i));
			sb2.append("'}");
			if(i < cc)
				sb2.append(",");
		}
		out.println("<script>");
		out.println("colNames=[\n"+sb.toString()+"\n];");
		out.println("colModel=[\n"+sb2.toString()+"\n];");
		out.println("</script>");
		//
		sb = new StringBuffer(); 
		while(rs.next()){
			sb.append("{");
			for(int i=1; i<=cc; i++){
				int type = meta.getColumnType(i);
				sb.append("\""+meta.getColumnName(i)+"\"");
				sb.append(":\"");
				String str = rs.getString(i);
				
				Integer TYPE_BLOB=null;
				Integer TYPE_BINARY=null;
				Integer TYPE_LONGVARBINARY=null;
				Integer TYPE_VARBINARY=null;
				Integer TYPE_JAVA_OBJECT=null;
				Integer TYPE_CLOB=null;
				Integer TYPE_NCLOB=null;
				Integer TYPE_SQLXML=null;
				Integer TYPE_LONGVARCHAR=null;
				Integer TYPE_LONGNVARCHAR=null;

				/**
				 * JDK 1.5 버젼과의 호환성 유지를 위해 추가함.
				 * 1.5 버젼은 NCLOB, SQLXML, LONGNVARCHAR 등이 없으므로 reflect 를 이용해 처리함.
				 **/
				Field[] fields = Types.class.getFields();
				for(Field field : fields) {
					if("BLOB".equals(field.getName())) {
						TYPE_BLOB = (Integer)field.get(null);
					} else if("BINARY".equals(field.getName())) {
						TYPE_BINARY = (Integer)field.get(null);
					} else if("LONGVARBINARY".equals(field.getName())) {
						TYPE_LONGVARBINARY = (Integer)field.get(null);
					} else if("VARBINARY".equals(field.getName())) {
						TYPE_VARBINARY = (Integer)field.get(null);
					} else if("JAVA_OBJECT".equals(field.getName())) {
						TYPE_JAVA_OBJECT = (Integer)field.get(null);
					} else if("CLOB".equals(field.getName())) {
						TYPE_CLOB = (Integer)field.get(null);
					} else if("NCLOB".equals(field.getName())) {
						TYPE_NCLOB = (Integer)field.get(null);
					} else if("SQLXML".equals(field.getName())) {
						TYPE_SQLXML = (Integer)field.get(null);
					} else if("LONGVARCHAR".equals(field.getName())) {
						TYPE_LONGVARCHAR = (Integer)field.get(null);
					} else if("LONGNVARCHAR".equals(field.getName())) {
						TYPE_LONGNVARCHAR = (Integer)field.get(null);
					}
				}

				if(str != null){
					sb.append(str.replaceAll("\t","&#09;").replaceAll("\r\n","&#13;&#10;").replaceAll("\r","&#13;&#10;").replaceAll("\n","&#13;&#10;").replaceAll("\\\\","&#92;").replaceAll("\"","&#34;").replaceAll("\'","&#39;"));

				} else if (type==TYPE_BLOB || type==TYPE_BINARY || type==TYPE_LONGVARBINARY || 
						type==TYPE_VARBINARY || type==TYPE_JAVA_OBJECT || type==TYPE_CLOB || type==TYPE_NCLOB || 
						type==TYPE_SQLXML || type==TYPE_LONGVARCHAR || type==TYPE_LONGNVARCHAR){
					sb.append("[Long DATA]");
				} else {
					sb.append("[NULL]");
				}
				sb.append("\"");
				if(i < cc)
					sb.append(",");
			}
			sb.append("},\n");
		}
		String str = sb.toString();
		out.println("<script>");
		out.println("mydata=[\n");
		if(str.length() > 0){
			out.println(str.substring(0, str.length()-2));
		}
		out.println("\n]");
		%>
		$("#sqlResult").jqGrid({
			datatype: "local",
			height: 250,
		   	colNames:colNames,
		   	colModel:colModel,
		   	width: 740
		});
		
		for(var i=0; i<=mydata.length; i++){
			if(mydata[i]){
				$("#sqlResult").jqGrid('addRowData',i+1,mydata[i]);
			}
		}

		<%
		out.println("</script>");
	}
}

dbContext.close();
/* if(rs != null){
	try{
		rs.close();
	}catch(Exception e){ }
} */
/* if(extConn != null){
	try{
		extConn.close();
	}catch(Exception e){ }
} */
%>
