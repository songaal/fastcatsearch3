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
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.service.KeywordService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.settings.IRSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CatServerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private IRService irService;
	private JobService jobController;
	private DBService dbHandler;
	private KeywordService keywordService;
	public static long startTime;
	private static Logger logger;
	
    public CatServerServlet(){}

    public void init() {
    	String ServerHome = getServletContext().getRealPath("/WEB-INF/search");
		System.out.println("Init ServerHome = "+ServerHome);
		String home;
		home = getServletConfig().getInitParameter("fastcat_manage_root");
		if(home != null){ 
			getServletContext().setAttribute("FASTCAT_MANAGE_ROOT", getServletContext().getContextPath()+home); 
		}
		
		home = getServletConfig().getInitParameter("fastcat_search_root");
		if(home != null){ 
			getServletContext().setAttribute("FASTCAT_SEARCH_ROOT", getServletContext().getContextPath()+home); 
		}
		
		File f = new File(ServerHome);
		if(!f.exists()){
			System.err.println("Error! Path \""+ServerHome+"\" is not exist!");
			System.exit(1);
		}
		
		logger = LoggerFactory.getLogger(CatServerServlet.class);
		
		IRSettings.setHome(ServerHome);
		dbHandler = DBService.getInstance();
		keywordService = KeywordService.getInstance();
		jobController = JobService.getInstance();
		irService = IRService.getInstance();
		
		try {
			start();
		} catch (ServiceException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public void start() throws ServiceException{
		
		dbHandler.start();
		jobController.start();
		
		irService.start();
		keywordService.start();
		
		startTime = System.currentTimeMillis();
		logger.info("CatServer started!");
	}
	
	public void stop() throws ServiceException{
		keywordService.stop();
		irService.stop();
		jobController.stop();
		dbHandler.stop();
		
		logger.info("CatServer shutdown!");
	}
	
	public void destroy() {
		dbHandler = null;
		irService = null;
		jobController = null;
		keywordService = null;
		logger.info("CatServer destroy!");
	}
}
