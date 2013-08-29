<%@ page contentType="text/html; charset=UTF-8"%> 

<%@page import="java.util.List"%>
<%@page import="java.net.*"%>
<%@page import="java.io.*"%>

<%@include file="../common.jsp" %>


<%

	String urlString = request.getParameter("_searchHost");

	String cn = request.getParameter("cn");
	String sd = request.getParameter("sd");
	String sn = request.getParameter("sn");
	String ln = request.getParameter("ln");
	String fl = request.getParameter("fl");
	String se = request.getParameter("se");
	String ra = request.getParameter("ra");
	String ft = request.getParameter("ft");
	String gr = request.getParameter("gr");
	String gf = request.getParameter("gf");
	String ht = request.getParameter("ht");
	String so = request.getParameter("so");
	String ud = request.getParameter("ud");
	String timeout = request.getParameter("timeout");
	
	String paramString =  
			"cn=" + cn
			+ "&sd=" + sd
			+ "&sn=" + sn
			+ "&ln=" + ln
			+ "&fl=" + fl
			+ "&se=" + se
			+ "&ra=" + ra
			+ "&ft=" + ft
			+ "&gr=" + gr
			+ "&gf=" + gf
			+ "&ht=" + ht
			+ "&so=" + so
			+ "&ud=" + ud
			+ "&timeout=" + timeout
			;
	URL url = new URL(urlString);
	URLConnection connection = url.openConnection();
	connection.setDoOutput(true);

	OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
	output.write(paramString);
	output.close();
	
	BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	String decodedString = null;
	while ((decodedString = in.readLine()) != null) {
		out.write(decodedString);
	}
	in.close();
	
%>