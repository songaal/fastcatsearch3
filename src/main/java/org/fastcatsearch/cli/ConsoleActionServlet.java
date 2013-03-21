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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fastcatsearch.cli.CommandResult.Status;
import org.fastcatsearch.cli.command.ListCollectionCommand;
import org.fastcatsearch.util.ClassDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;


public class ConsoleActionServlet extends HttpServlet {
	
	private static final long serialVersionUID = 4849511865192716149L;
	private static final Logger logger = LoggerFactory.getLogger(ConsoleActionServlet.class);

	String[] CMD_INFO_SYSTEM = new String[]{"sysinfo"}; //시스템 정보.
	String[] CMD_SHOW_DICTIONARY = new String[]{"show", "dictionary"};
	String[] CMD_SHOW_SETTING = new String[]{"show", "setting"}; //fastcat.conf 셋팅.
	
	String[] CMD_LIST_COLLECTION = new String[]{"list", "collection"};
	String[] CMD_INFO_COLLECTION = new String[]{"info"}; //컬렉션정보-색인크기, 위치등..
	
	String[] CMD_SHOW_SCHEMA = new String[]{"show", "schema"};
	String[] CMD_SHOW_DATASOURCE = new String[]{"show", "datasource"};
	
	String[] CMD_START_FULLINDEX = new String[]{"start", "index", "full"};
	String[] CMD_START_INCINDEX = new String[]{"start", "index", "inc"};
	String[] CMD_STATUS_FULLINDEX = new String[]{"status", "index", "full"};
	String[] CMD_STATUS_INCINDEX = new String[]{"status", "index", "inc"};
	
	String[] CMD_SET_SCHEDULE_FULLINDEX = new String[]{"set", "schedule", "full"};
	String[] CMD_SET_SCHEDULE_INCINDEX = new String[]{"set", "schedule", "inc"};
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		List<Class> clsList = this.detectCommands();
		
		for(Class cls : clsList) {
			
			logger.debug("class : {} ",cls.getName());
			
		}
		
		
		String command = request.getParameter("command");
		String[] commandList = command.split(" ");
		
		if(command == null || command.length() == 0){
			responseError(response, "Command is empty!");
			return;
		}
		
		CommandResult result = null;
		
		if(isCommand(CMD_INFO_SYSTEM, commandList)){
			
			
		}else if(isCommand(CMD_SHOW_DICTIONARY, commandList)){
			result = new IndexCollectionCommand().doCommand();
			
		}else if(isCommand(CMD_LIST_COLLECTION , commandList)){
			result = new ListCollectionCommand().doCommand();
		}else{
			responseError(response, "Unknown Command : "+command);
			return;
		}
		/*
		 * TODO
		 * 
		 * 각 COMMAND들을 구현한다.
		 * */
		
		
		if(result == null){
			responseError(response, "No result!");
		}else{
			reponseResult(response, result);
		}
		
    }

	private boolean isCommand(String[] expected, String[] actual){
		for (int i = 0; i < expected.length; i++) {
			if(!expected[i].equalsIgnoreCase(actual[i])){
				return false;
			}
		}
		return true;
	}
	
	private void responseError(HttpServletResponse response, String errorMessage) throws IOException {
		//http.write
		Gson gson = new Gson();
		response.getWriter().write("ERROR\n"+errorMessage);
	}
	
	private void reponseResult(HttpServletResponse response, CommandResult message) throws IOException{
		//첫줄에 Fail, warning, success를 구분하여 표시한다.
		response.getWriter().write(message.status.name()+"\n"+message.result);	
	}
	
	@SuppressWarnings("rawtypes")
	public List<Class> detectCommands() {
		ClassDetector<Class> detector = new ClassDetector<Class>() {
			@Override
			public Class classify(String ename, String pkg) {
				if(ename.endsWith(".class")) {
					ename = ename.substring(0,ename.length()-6);
					ename = ename.replaceAll("/", ".");
					if(ename.startsWith(pkg)) {
						try {
							Class<?> cls = Class.forName(ename);
							cls.asSubclass(Command.class);
							return cls;
						} catch (ClassNotFoundException e) { }
					}
				}
				return null;
			}
		};
		return detector.detectClass("org.fastcatsearch.cli.command.");
	}
}
