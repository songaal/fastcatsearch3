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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.fastcatsearch.cli.ConsoleActionServlet;
import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.servlet.CSVMakeServlet;
import org.fastcatsearch.servlet.DocumentListServlet;
import org.fastcatsearch.servlet.DocumentSearchServlet;
import org.fastcatsearch.servlet.GroupSearchServlet;
import org.fastcatsearch.servlet.IndexingInfoServlet;
import org.fastcatsearch.servlet.ManagementInfoServlet;
import org.fastcatsearch.servlet.PopularKeywordServlet;
import org.fastcatsearch.servlet.RecommendKeywordServlet;
import org.fastcatsearch.servlet.RegisterJobServlet;
import org.fastcatsearch.servlet.SearchEventListServlet;
import org.fastcatsearch.servlet.SearchKeywordListServlet;
import org.fastcatsearch.servlet.SearchMonServlet;
import org.fastcatsearch.servlet.SearchServlet;
import org.fastcatsearch.servlet.StatisticsInfoServlet;
import org.fastcatsearch.servlet.SystemMonServlet;
import org.fastcatsearch.servlet.WebServiceHttpServlet;
import org.fastcatsearch.servlet.cluster.ClusterGroupSearchServlet;
import org.fastcatsearch.servlet.cluster.ClusterSearchServlet;
import org.fastcatsearch.settings.Settings;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.resource.Resource;
import org.mortbay.thread.QueuedThreadPool;
import org.mortbay.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;



public class WebService extends AbstractService{
	private static Logger logger = LoggerFactory.getLogger(WebService.class);
	
	//always use Root context
	private String CLUSTER_CONTEXT = "/cluster";
	private String SEARCH_CONTEXT = "/search";
	private String GROUP_CONTEXT = "/group";
	private String KEYWORD_CONTEXT = "/keyword";
	private String EXECUTE_CONTEXT = "/execute";
	private String DOCUMENT_LIST_CONTEXT = "/doclist";
	private String DOCUMENT_SEARCH_CONTEXT = "/docsearch";
	private String MONITORING_CONTEXT = "/monitoring";
	private String CONSOLE_CONTEXT = "/console";
	
	private Server server;
	private int SERVER_PORT;
	
	private static WebService instance;
	private HandlerList handlerList;
	
	
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
		
		if(System.getProperty("server.port")!=null) {
			SERVER_PORT = Integer.parseInt(System.getProperty("server.port"));
		} else {
			SERVER_PORT = settings.getInt("port");
		}
		
		Server server = new Server(SERVER_PORT);
		handlerList = new HandlerList();
		
		try {
			
			Resource configXml = Resource.newResource(environment.filePaths().getPath("conf/webAdmin.xml"));
			if(configXml != null){
		        XmlConfiguration configuration = new XmlConfiguration(configXml.getInputStream());
		        if(configuration != null){
			        WebAppContext webapp = (WebAppContext)configuration.configure();
			        File workDir = environment.filePaths().getFile("work");
					if(workDir.exists()){
						try {
							FileUtils.deleteDirectory(workDir);
						} catch (IOException ignore) {
						}			
					}
					webapp.setTempDirectory(workDir);
					webapp.getServletContext().setAttribute("FASTCAT_MANAGE_ROOT", webapp.getContextPath());
					webapp.getServletContext().setAttribute("FASTCAT_SEARCH_ROOT", "");
					
			        handlerList.addHandler(webapp);
		        }
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		
		
		// Cluster search ServletContextHandler
		final Context context0 = new Context(server, CLUSTER_CONTEXT, Context.SESSIONS);
		context0.setMaxFormContentSize(10 * 1024 * 1024); //파라미터전송 10MB까지 가능.
		context0.addServlet(new ServletHolder(new ClusterGroupSearchServlet(WebServiceHttpServlet.JSON_TYPE)),"/group/json");
		context0.addServlet(new ServletHolder(new ClusterGroupSearchServlet(WebServiceHttpServlet.XML_TYPE)),"/group/xml");
		context0.addServlet(new ServletHolder(new ClusterSearchServlet(WebServiceHttpServlet.JSON_TYPE)),"/search/json");
		context0.addServlet(new ServletHolder(new ClusterSearchServlet(WebServiceHttpServlet.XML_TYPE)),"/search/xml");
		handlerList.addHandler(context0);
				
		// Search ServletContextHandler
		final Context context = new Context(server, SEARCH_CONTEXT, Context.SESSIONS);
		context.setMaxFormContentSize(10 * 1024 * 1024); //파라미터전송 10MB까지 가능.
		context.addServlet(new ServletHolder(new SearchServlet(SearchServlet.JSON_TYPE)),"/json");
		context.addServlet(new ServletHolder(new SearchServlet(SearchServlet.JSONP_TYPE)),"/jsonp");
		context.addServlet(new ServletHolder(new SearchServlet(SearchServlet.XML_TYPE)),"/xml");
		context.addServlet(new ServletHolder(new SearchServlet(SearchServlet.IS_ALIVE)),"/isAlive");
		handlerList.addHandler(context);
		
		/*
		 * 그룹핑 전용검색.
		 * */
		final Context context1 = new Context(server, GROUP_CONTEXT, Context.SESSIONS);
		context1.setMaxFormContentSize(10 * 1024 * 1024); //파라미터전송 10MB까지 가능.
		context1.addServlet(new ServletHolder(new GroupSearchServlet(SearchServlet.JSON_TYPE)),"/json");
		context1.addServlet(new ServletHolder(new GroupSearchServlet(SearchServlet.JSONP_TYPE)),"/jsonp");
		context1.addServlet(new ServletHolder(new GroupSearchServlet(SearchServlet.XML_TYPE)),"/xml");
		handlerList.addHandler(context1);
		
		
		final Context context2 = new Context(server, EXECUTE_CONTEXT, Context.SESSIONS);
		context2.addServlet(new ServletHolder(new RegisterJobServlet(WebServiceHttpServlet.JSON_TYPE)),"/");
		handlerList.addHandler(context2);
		
        // ServletContextHandler
		final Context context3 = new Context(server, KEYWORD_CONTEXT, Context.SESSIONS);
		context3.setClassLoader(this.getClass().getClassLoader());
		context3.addServlet(new ServletHolder(new RecommendKeywordServlet()),"/recommend");
		context3.addServlet(new ServletHolder(new RecommendKeywordServlet(WebServiceHttpServlet.JSON_TYPE)),"/recommend/json");
		context3.addServlet(new ServletHolder(new RecommendKeywordServlet(WebServiceHttpServlet.JSONP_TYPE)),"/recommend/jsonp");
		context3.addServlet(new ServletHolder(new RecommendKeywordServlet(WebServiceHttpServlet.XML_TYPE)),"/recommend/xml");
		context3.addServlet(new ServletHolder(new PopularKeywordServlet(WebServiceHttpServlet.JSON_TYPE)),"/popular");
		context3.addServlet(new ServletHolder(new PopularKeywordServlet(WebServiceHttpServlet.JSON_TYPE)),"/popular/json");
		context3.addServlet(new ServletHolder(new PopularKeywordServlet(WebServiceHttpServlet.JSONP_TYPE)),"/popular/jsonp");
		context3.addServlet(new ServletHolder(new PopularKeywordServlet(WebServiceHttpServlet.XML_TYPE)),"/popular/xml");
		handlerList.addHandler(context3);
		
        // DOCUMENT_LIST_CONTEXT
		final Context context4 = new Context(server, DOCUMENT_LIST_CONTEXT, Context.SESSIONS);
		context4.addServlet(new ServletHolder(new DocumentListServlet(DocumentListServlet.JSON_TYPE)),"/json");
		context4.addServlet(new ServletHolder(new DocumentListServlet(DocumentListServlet.XML_TYPE)),"/xml");
		handlerList.addHandler(context4);
		
		// DOCUMENT_SEARCH_CONTEXT
		final Context context5 = new Context(server, DOCUMENT_SEARCH_CONTEXT, Context.SESSIONS);
		context5.addServlet(new ServletHolder(new DocumentSearchServlet(DocumentSearchServlet.JSON_TYPE)),"/json");
		context5.addServlet(new ServletHolder(new DocumentSearchServlet(DocumentSearchServlet.XML_TYPE)),"/xml");
		handlerList.addHandler(context5);
		
		
		//7. 모니터링 MONITORING_CONTEXT
		final Context context6 = new Context(server, MONITORING_CONTEXT, Context.SESSIONS);
		context6.setClassLoader(this.getClass().getClassLoader());
		//시스템정보
		context6.addServlet(new ServletHolder(new ManagementInfoServlet(WebServiceHttpServlet.JSON_TYPE)),"/system");
		context6.addServlet(new ServletHolder(new ManagementInfoServlet(WebServiceHttpServlet.JSONP_TYPE)),"/system/jsonp");
		
		context6.addServlet(new ServletHolder(new SystemMonServlet(WebServiceHttpServlet.JSON_TYPE)),"/system/detail");
		context6.addServlet(new ServletHolder(new SystemMonServlet(WebServiceHttpServlet.JSONP_TYPE)),"/system/detail/jsonp");
		
		//검색통계
		context6.addServlet(new ServletHolder(new StatisticsInfoServlet(WebServiceHttpServlet.JSON_TYPE)),"/search");
		context6.addServlet(new ServletHolder(new StatisticsInfoServlet(WebServiceHttpServlet.JSONP_TYPE)),"/search/jsonp");
		
		context6.addServlet(new ServletHolder(new SearchMonServlet(WebServiceHttpServlet.JSON_TYPE)),"/search/detail");
		context6.addServlet(new ServletHolder(new SearchMonServlet(WebServiceHttpServlet.JSONP_TYPE)),"/search/detail/jsonp");
		
		//검색엔진정보
		context6.addServlet(new ServletHolder(new IndexingInfoServlet(WebServiceHttpServlet.JSON_TYPE)),"/indexing");
		context6.addServlet(new ServletHolder(new IndexingInfoServlet(WebServiceHttpServlet.JSONP_TYPE)),"/indexing/jsonp");
		//실시간 검색키워드 SearchKeywordListServlet
		context6.addServlet(new ServletHolder(new SearchKeywordListServlet(WebServiceHttpServlet.JSON_TYPE)),"/keywordList");
		context6.addServlet(new ServletHolder(new SearchKeywordListServlet(WebServiceHttpServlet.JSONP_TYPE)),"/keywordList/jsonp");
		//이벤트 내역
		context6.addServlet(new ServletHolder(new SearchEventListServlet()),"/eventList");
		//csv파일
		context6.addServlet(new ServletHolder(new CSVMakeServlet()),"/csv");
		handlerList.addHandler(context6);
		
		
		final Context context7 = new Context(server, CONSOLE_CONTEXT, Context.SESSIONS);
		context7.setClassLoader(this.getClass().getClassLoader());
		context7.addServlet(new ServletHolder(new ConsoleActionServlet()), "/command");
		handlerList.addHandler(context7);
		
		
		
		
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
	
	public void addHandler(Handler handler){
		handlerList.addHandler(handler);
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
