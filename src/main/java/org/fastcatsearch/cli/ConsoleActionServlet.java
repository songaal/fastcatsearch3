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
	
	private static final List<Command> commandActionList = new ArrayList<Command>();

	@Override
	public void init() throws ServletException {
		super.init();
		commandActionList.clear();
		commandActionList.addAll(detectCommands());
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String command = request.getParameter("command");
		String[] commandArrray = command.split(" ");
		
		if(command == null || command.length() == 0){
			responseError(response, "Command is empty!");
			return;
		}
		
		CommandResult result = null;
		
		for(Command commandAction : commandActionList) {
			if(commandAction.isCommand(commandArrray)) {
				try {
					result = commandAction.getClass().newInstance().doCommand();
				} catch (InstantiationException e) {
				} catch (IllegalAccessException e) {
				}
				break;
			}
		}
		
		if(result == null) {
			responseError(response, "No result!");
		} else {
			reponseResult(response, result);
		}
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
	
	public List<Command> detectCommands() {
		ClassDetector<Command> detector = new ClassDetector<Command>() {
			@Override
			public Command classify(String ename, String pkg) {
				if(ename.endsWith(".class")) {
					ename = ename.substring(0,ename.length()-6);
					ename = ename.replaceAll("/", ".");
					if(ename.startsWith(pkg)) {
						try {
							Object inst = Class.forName(ename).newInstance();
							if(inst instanceof Command) {
								return (Command)inst;
							}
						} catch (ClassNotFoundException e) { 
						} catch (InstantiationException e) {
						} catch (IllegalAccessException e) {
						}
					}
				}
				return null;
			}
		};
		return detector.detectClass("org.fastcatsearch.cli.command.");
	}
}
