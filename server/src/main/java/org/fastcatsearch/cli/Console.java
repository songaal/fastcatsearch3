/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.cli;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CLI환경에서 명령을 내리는 프로그램.
 * 
 * [Usage]java Console 192.168.0.100 8080 과 같이 접속하여 사용한다.
 * 
 * @see ConsoleActionServlet
 */

public class Console {
	
	String host;
	int port;
	
	String[] CMD_USE_COLLECTION = new String[]{"use"};
	
	List<String> history;
	
	String currentCollection;
	
	public Console(String host, int port) {
		history = new ArrayList<String>();
		this.host = host;
		this.port = port;
	}
	
	/**
	 * 
	 * First Of All You Must Call "use ${Collection-Name} "  And Use It (Collection) Until Session Close
	 * 
	 * */
	public static void main(String[] args) {
		
		
		if(args.length < 2){
			printUsage();
			System.exit(0);
		} else {
			printLicenseHeader();
		}
		
		String host = args[0];
		String portStr = args[1];
		
		int port = 8080; // default port
		
		try {
			port = Integer.parseInt(portStr);
		} catch (NumberFormatException e) {
			System.out.println("port number is not numeric");
			printUsage();
			System.exit(0);
		}
		
		Console console = new Console(host,port);
		console.interpret();
	}
	
	public void interpret() {
		
		//
		// Real Command Logic Described To ConsoleActionServlet.java
		// It Uses Simple Interpret Logic Without Call-Wait Thread 
		// 
		
		//
		// Show One Of Two Prompt When Command Phrase Complete Or Not
		// (Like Mysql Prompt)
		//
		String[] prompt = new String[] {
			"fastcatsearch> ", 
			"            -> "
		};
		boolean completed = true;
		StringBuilder cmdBuf = new StringBuilder();
		err = System.err;
		
		while(true) {
			String cmd = readLine(
					completed?prompt[0]:prompt[1]);
			//
			// Command Phrase Will Completed When ';' Appears At End
			// Except System Command ( help, exit ...)
			//
			if(cmd.endsWith(";")) {
				cmd = cmd.substring(0,cmd.length() -1);
				completed = true;
			} else {
				completed = false;
			}
			cmdBuf.append(cmd);
			if(cmdBuf.length()==0) {
				completed = true;
			}
			
			cmd = cmdBuf.toString().trim();

			//
			// System Command ( help, exit ... )
			//
			if(cmd.equals("help")) {
				//printHelp();
				//cmdBuf.setLength(0);
				completed = true;
				//continue;
			} else if(cmd.equals("exit")) {
				System.exit(1);
			}
			
			//
			// Append Command Buffer When Command Phrase Not Completed
			//
			if(!completed) {
				cmdBuf.append(" ");
			} else {
				cmdBuf.setLength(0);
			}
			//
			// Execute Command (Completed Command Phrase)
			//
			if(completed) {
				if(!"".equals(cmd)) {
					String result = communicate(cmd);
					//우선 임시로 json string 을 그대로 출력하도록 한다.
					printf("command : %s \nresult : \n%s\n", cmd, result);
				}
			}
		}
	}
	
	private HttpClient httpClient;
	private HttpPost httpPost;
	private HttpResponse httpResponse;
	private PrintStream err;
	
	public String communicate (String command) {
		
		if(httpClient==null) {
			httpClient = new DefaultHttpClient();
			String url = "http://"+host+":"+port+"/console/command";
			httpPost = new HttpPost(url);
		}
		
		InputStreamReader ir = null;
		BufferedReader br = null;
		String result = null;
		try {
			if(httpPost!=null) {
				List<NameValuePair>npList = new ArrayList<NameValuePair>();
				npList.add(new BasicNameValuePair("command", command));
				httpPost.setHeader("Content-type","application/x-www-form-urlencoded");
				httpPost.setEntity(new UrlEncodedFormEntity(npList, "UTF-8"));
				httpResponse = httpClient.execute(httpPost);
				StringBuffer sb = new StringBuffer();
				if(httpResponse != null){
					ir = new InputStreamReader(httpResponse.getEntity().getContent(),"UTF-8");
					br = new BufferedReader(ir);
					int inx=0;
					for(String rline; (rline = br.readLine()) !=null; inx++) {
						if(inx==0) {
							result = rline;
						} else {
							sb.append(rline).append("\n");
						}
					}
					
					if("ERROR".equals(result)) {
						
					} else if("SUCCESS".equals(result)) {
						
					} else {
						
					}
				}
				return sb.toString();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(err);
		} catch (ClientProtocolException e) {
			e.printStackTrace(err);
		} catch (IOException e) {
			e.printStackTrace(err);
		} catch (NullPointerException e) {
			e.printStackTrace(err);
		} finally {
			if(br!=null) {
				try {
					br.close();
				} catch (IOException e) {
				}
			}
			if(ir!=null) {
				try {
					ir.close();
				} catch (IOException e) {
				}
			}
		}
		
		return null;
	}

	private static void printLicenseHeader() {

		System.out.println("###########################################");
		System.out.println("# Copyright FastSearch. GPL 2.0 License.");
		System.out.println("# FastcatSearch CLI Tool");
		System.out.println("###########################################");
	}

	private static void printUsage() {
		
		System.err.println("[Usage] java Console [host] [port]");
	}
	
	private static void printHelp() {
		
		System.out.println("\nhelp : \n");
	}

	/**
	 * Read Input Line With Showing Prompt
	 * @param prompt
	 * @return
	 */
	private static String readLine(String prompt) {
		String line = null;
//		java.io.Console c = System.console();
//		if (c != null) {
//			line = c.readLine(prompt);
//		} else {
			// For Eclipse User
			System.out.print(prompt);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			try {
				line = bufferedReader.readLine();
			} catch (IOException ignore) { 
			}
//		}
		return line;
	}
	
	/**
	 * Printout Formatted String
	 * @param format
	 * @param args
	 */
	private static void printf(String format, Object... args) {
//		java.io.Console c = System.console();
//		if (c != null) {
//			c.printf(format, args);
//		} else {
			// For Eclipse User
			System.out.print(String.format(format, args));
//		}
	}
}
