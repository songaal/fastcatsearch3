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

import org.fastcatsearch.control.JobController;
import org.fastcatsearch.db.DBHandler;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.server.PreparedLoader;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.KeywordService;
import org.fastcatsearch.service.QueryCacheService;
import org.fastcatsearch.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CatServerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private IRService irService;
	private JobController jobController;
	private DBHandler dbHandler;
	private KeywordService keywordService;
	private PreparedLoader preparedLoader;
	private QueryCacheService cacheService;
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
		dbHandler = DBHandler.getInstance();
		keywordService = KeywordService.getInstance();
		jobController = JobController.getInstance();
		irService = IRService.getInstance();
		cacheService = QueryCacheService.getInstance();
		preparedLoader = new PreparedLoader();
		
		try {
			start();
		} catch (ServiceException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public void start() throws ServiceException{
		
		dbHandler.start();
		jobController.start();
		
		preparedLoader.load();
		
		irService.start();
		keywordService.start();
		cacheService.start();
		
		startTime = System.currentTimeMillis();
		logger.info("CatServer started!");
	}
	
	public void stop() throws ServiceException{
		preparedLoader.unload();
		keywordService.shutdown();
		irService.shutdown();
		jobController.shutdown();
		dbHandler.shutdown();
		cacheService.shutdown();
		
		logger.info("CatServer shutdown!");
	}
	
	public void destroy() {
		dbHandler = null;
		irService = null;
		jobController = null;
		keywordService = null;
		preparedLoader = null;
		cacheService = null;
		logger.info("CatServer destroy!");
	}
}
