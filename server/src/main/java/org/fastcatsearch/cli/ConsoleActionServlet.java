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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.fastcatsearch.env.Environment;
import org.fastcatsearch.util.ClassDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleActionServlet extends HttpServlet {
	
	private static final long serialVersionUID = 4849511865192716149L;
	private static final Logger logger = LoggerFactory.getLogger(ConsoleActionServlet.class);
	
	private static final List<Command> commandActionList = new ArrayList<Command>();
	
	private ConsoleActionContext context;

	protected Environment environment;
	
	
	@Override
	public void init() throws ServletException {
		super.init();
		commandActionList.clear();
		commandActionList.addAll(detectCommands());
		logger.trace("detected command list : {}", commandActionList);
		//environment는 셋팅이 안된다.
	}
	
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(context == null || !request.getSession().equals(context.session)) {
			context = new ConsoleActionContext(request.getSession());
		}
		request.setCharacterEncoding("UTF-8");
		String command = request.getParameter("command");
		String[] commandArrray = command.split(" ");
		CommandResult result = null;
		if(command == null || command.length() == 0) {
			this.responseResult(response,new CommandResult("Command is empty!", CommandResult.Status.ERROR));
			return;
		}
		
		try {
		
			for(Command commandAction : commandActionList) {
				if(commandAction.isCommand(commandArrray)) {
					try {
						Command newCommand = commandAction.getClass().newInstance();
						newCommand.setEnvironment(environment);
						result = newCommand.doCommand(commandArrray, context);
					} catch (InstantiationException e) {
						logger.error("",e);
					} catch (IllegalAccessException e) {
						logger.error("",e);
					}
					break;
				}
			}
		} catch (CommandException e) {
			result = e.getCommandResult();
		}
		
		if(result == null) {
			result = new CommandResult("No result!", CommandResult.Status.ERROR);
		}
		responseResult(response, result);
    }
	
	private void responseResult(HttpServletResponse response, CommandResult message) throws IOException {
		//첫줄에 Fail, warning, success를 구분하여 표시한다.
		response.setContentType("text/html; charset=utf-8");
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
								logger.trace("instance {} detected", inst);								
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

class ConsoleActionContext implements ConsoleSessionContext {
	
	HttpSession session;
	
	public ConsoleActionContext(HttpSession session) {
		this.session = session;
	}
	
	@Override
	public void setAttribute(String key, Object value) {
		session.setAttribute(key, value);
	}

	@Override
	public Object getAttribute(String key) {
		return session.getAttribute(key);
	}
}
