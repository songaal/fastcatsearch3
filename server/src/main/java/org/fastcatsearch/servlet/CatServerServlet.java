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

package org.fastcatsearch.servlet;

import java.io.File;

import javax.servlet.http.HttpServlet;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CatServerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private IRService irService;
	private JobService jobController;
	private DBService dbHandler;
	public static long startTime;
	private static Logger logger;
	
    public CatServerServlet(){}

    public void init() {
    	String serverHome = getServletContext().getRealPath("/WEB-INF/search");
		System.out.println("Init ServerHome = "+serverHome);
		String home;
		home = getServletConfig().getInitParameter("fastcat_manage_root");
		if(home != null){ 
			getServletContext().setAttribute("FASTCAT_MANAGE_ROOT", getServletContext().getContextPath()+home); 
		}
		
		home = getServletConfig().getInitParameter("fastcat_search_root");
		if(home != null){ 
			getServletContext().setAttribute("FASTCAT_SEARCH_ROOT", getServletContext().getContextPath()+home); 
		}
		
		File f = new File(serverHome);
		if(!f.exists()){
			System.err.println("Error! Path \""+serverHome+"\" is not exist!");
			System.exit(1);
		}
		
		logger = LoggerFactory.getLogger(CatServerServlet.class);
		
		try {
			Environment environment = new Environment(serverHome).init();
		} catch (FastcatSearchException e1) {
			e1.printStackTrace();
		}
		dbHandler = DBService.getInstance();
		jobController = JobService.getInstance();
//		irService = IRService.getInstance();
		
		try {
			start();
		} catch (FastcatSearchException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public void start() throws FastcatSearchException{
		
		dbHandler.start();
		jobController.start();
		
		irService.start();
		
		startTime = System.currentTimeMillis();
		logger.info("CatServer started!");
	}
	
	public void stop() throws FastcatSearchException{
		irService.stop();
		jobController.stop();
		dbHandler.stop();
		
		logger.info("CatServer shutdown!");
	}
	
	public void destroy() {
		dbHandler = null;
		irService = null;
		jobController = null;
		logger.info("CatServer destroy!");
	}
}
