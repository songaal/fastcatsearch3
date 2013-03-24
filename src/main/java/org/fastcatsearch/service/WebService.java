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

package org.fastcatsearch.service;

import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.servlet.AggregationSearchServlet;
import org.fastcatsearch.servlet.DocumentListServlet;
import org.fastcatsearch.servlet.DocumentSearchServlet;
import org.fastcatsearch.servlet.PopularKeywordServlet;
import org.fastcatsearch.servlet.RegisterJobServlet;
import org.fastcatsearch.servlet.SearchServlet;
import org.fastcatsearch.servlet.WebServiceHttpServlet;
import org.fastcatsearch.settings.Settings;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class WebService extends AbstractService{
	private static Logger logger = LoggerFactory.getLogger(WebService.class);
	
	//always use Root context
	private String SEARCH_CONTEXT = "/search";
	private String GROUP_CONTEXT = "/group";
	private String KEYWORD_CONTEXT = "/keyword";
	private String EXECUTE_CONTEXT = "/execute";
	private String DOCUMENT_LIST_CONTEXT = "/doclist";
	private String DOCUMENT_SEARCH_CONTEXT = "/docsearch";
	
	private Server server;
	private int SERVER_PORT;
	private JobExecutor jobExecutor;
	
	private static WebService instance;
	
	public static WebService getInstance(){
		return instance;
	}
	public void asSingleton() {
		instance = this;
	}
	
	public WebService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
		
	}
	
	protected boolean doStart() throws ServiceException{
		this.jobExecutor = serviceManager.getService(JobService.class);
		
//		IRConfig config = IRSettings.getConfig();
		if(System.getProperty("server.port")!=null) {
			SERVER_PORT = Integer.parseInt(System.getProperty("server.port"));
		} else {
			SERVER_PORT = settings.getInt("port");
		}
		
		server = new Server(SERVER_PORT);
		HandlerList handlerList = new HandlerList();
		
		// Search ServletContextHandler
		final Context context = new Context(server, SEARCH_CONTEXT, Context.SESSIONS);
		context.setMaxFormContentSize(10 * 1024 * 1024); //파라미터전송 10MB까지 가능.
		context.addServlet(new ServletHolder(new SearchServlet(SearchServlet.JSON_TYPE, jobExecutor)),"/json");
		context.addServlet(new ServletHolder(new SearchServlet(SearchServlet.JSONP_TYPE, jobExecutor)),"/jsonp");
		context.addServlet(new ServletHolder(new SearchServlet(SearchServlet.XML_TYPE, jobExecutor)),"/xml");
		context.addServlet(new ServletHolder(new SearchServlet(SearchServlet.IS_ALIVE, jobExecutor)),"/isAlive");
		handlerList.addHandler(context);
		
		/*
		 * 그룹핑 전용검색.
		 * */
		final Context context1 = new Context(server, GROUP_CONTEXT, Context.SESSIONS);
		context1.setMaxFormContentSize(10 * 1024 * 1024); //파라미터전송 10MB까지 가능.
		context1.addServlet(new ServletHolder(new AggregationSearchServlet(SearchServlet.JSON_TYPE, jobExecutor)),"/json");
		context1.addServlet(new ServletHolder(new AggregationSearchServlet(SearchServlet.JSONP_TYPE, jobExecutor)),"/jsonp");
		context1.addServlet(new ServletHolder(new AggregationSearchServlet(SearchServlet.XML_TYPE, jobExecutor)),"/xml");
		handlerList.addHandler(context1);
		
		
		final Context context2 = new Context(server, EXECUTE_CONTEXT, Context.SESSIONS);
		context2.addServlet(new ServletHolder(new RegisterJobServlet(WebServiceHttpServlet.JSON_TYPE, jobExecutor)),"/");
		handlerList.addHandler(context2);
		
        // ServletContextHandler
		final Context context3 = new Context(server, KEYWORD_CONTEXT, Context.SESSIONS);
		context3.addServlet(new ServletHolder(new PopularKeywordServlet(PopularKeywordServlet.JSON_TYPE, jobExecutor)),"/popular");
		context3.addServlet(new ServletHolder(new PopularKeywordServlet(PopularKeywordServlet.JSON_TYPE, jobExecutor)),"/popular/json");
		context3.addServlet(new ServletHolder(new PopularKeywordServlet(PopularKeywordServlet.JSONP_TYPE, jobExecutor)),"/popular/jsonp");
		context3.addServlet(new ServletHolder(new PopularKeywordServlet(PopularKeywordServlet.XML_TYPE, jobExecutor)),"/popular/xml");
		handlerList.addHandler(context3);
		
        // DOCUMENT_LIST_CONTEXT
		final Context context4 = new Context(server, DOCUMENT_LIST_CONTEXT, Context.SESSIONS);
		context4.addServlet(new ServletHolder(new DocumentListServlet(DocumentListServlet.JSON_TYPE, jobExecutor)),"/json");
		context4.addServlet(new ServletHolder(new DocumentListServlet(DocumentListServlet.XML_TYPE, jobExecutor)),"/xml");
		handlerList.addHandler(context4);
		
		// DOCUMENT_SEARCH_CONTEXT
		final Context context5 = new Context(server, DOCUMENT_SEARCH_CONTEXT, Context.SESSIONS);
		context5.addServlet(new ServletHolder(new DocumentSearchServlet(DocumentSearchServlet.JSON_TYPE, jobExecutor)),"/json");
		context5.addServlet(new ServletHolder(new DocumentSearchServlet(DocumentSearchServlet.XML_TYPE, jobExecutor)),"/xml");
		handlerList.addHandler(context5);
		
		
        server.setHandler(handlerList);
        
		try {
			//stop을 명령하면 즉시 중지되도록.
			server.setStopAtShutdown(true);
			server.start();
			//Jetty는 2초후에 정지된다.
			if( server.getThreadPool() instanceof QueuedThreadPool ){
			   ((QueuedThreadPool) server.getThreadPool()).setMaxIdleTimeMs( 2000 );
			}
			logger.info("ServiceHandler Started! port = "+SERVER_PORT);
		} catch (Exception e) {
			throw new ServiceException(SERVER_PORT+" PORT로 웹서버 시작중 에러발생. ", e);
			
		}
		return true;
	}
	
	protected boolean doStop() throws ServiceException{
		try {
			logger.info("ServiceHandler stop requested...");
			server.stop();
			logger.info("Server Stop Ok!");
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		return true;
	}

	public int getClientCount() {
		return server.getConnectors().length;
	}

	@Override
	protected boolean doClose() throws ServiceException {
		return false;
	}
}
