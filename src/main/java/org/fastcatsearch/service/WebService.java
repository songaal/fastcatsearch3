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
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.servlet.AnalyzerServlet;
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
import org.fastcatsearch.servlet.TokenizerServlet;
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


public class WebService extends AbstractService{

	private static Logger logger = LoggerFactory.getLogger(WebService.class);
	
	//always use Root context
	private static final String CLUSTER_CONTEXT = "/cluster";
	private static final String SEARCH_CONTEXT = "/search";
	private static final String GROUP_CONTEXT = "/group";
	private static final String KEYWORD_CONTEXT = "/keyword";
	private static final String EXECUTE_CONTEXT = "/execute";
	private static final String DOCUMENT_LIST_CONTEXT = "/doclist";
	private static final String DOCUMENT_SEARCH_CONTEXT = "/docsearch";
	private static final String MONITORING_CONTEXT = "/monitoring";
	private static final String CONSOLE_CONTEXT = "/console";
	private static final String TOKENIZER_CONTEXT = "/tokenizer";
	private static final String ANALYZER_CONTEXT = "/analyzer";
	
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
		
		server = new Server(SERVER_PORT);
		handlerList = new HandlerList();
		
		try {
			
			Resource configXml = Resource.newResource(environment.filePaths().getPath("conf/webapp.xml"));
			if(configXml != null && configXml.exists()){
				logger.info("Load webapp >> {}", configXml.getFile().getAbsolutePath());
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
		final Context contextCluster = new Context(server, CLUSTER_CONTEXT, Context.SESSIONS);
		contextCluster.setMaxFormContentSize(10 * 1024 * 1024); //파라미터전송 10MB까지 가능.
		contextCluster.addServlet(new ServletHolder(new ClusterGroupSearchServlet(WebServiceHttpServlet.JSON_TYPE)),"/group/json");
		contextCluster.addServlet(new ServletHolder(new ClusterGroupSearchServlet(WebServiceHttpServlet.XML_TYPE)),"/group/xml");
		contextCluster.addServlet(new ServletHolder(new ClusterSearchServlet(WebServiceHttpServlet.JSON_TYPE)),"/search/json");
		contextCluster.addServlet(new ServletHolder(new ClusterSearchServlet(WebServiceHttpServlet.XML_TYPE)),"/search/xml");
		handlerList.addHandler(contextCluster);
				
		// Search ServletContextHandler
		final Context contextSearch = new Context(server, SEARCH_CONTEXT, Context.SESSIONS);
		contextSearch.setMaxFormContentSize(10 * 1024 * 1024); //파라미터전송 10MB까지 가능.
		contextSearch.addServlet(new ServletHolder(new SearchServlet(WebServiceHttpServlet.JSON_TYPE)),"/json");
		contextSearch.addServlet(new ServletHolder(new SearchServlet(WebServiceHttpServlet.JSONP_TYPE)),"/jsonp");
		contextSearch.addServlet(new ServletHolder(new SearchServlet(WebServiceHttpServlet.XML_TYPE)),"/xml");
		contextSearch.addServlet(new ServletHolder(new SearchServlet(SearchServlet.IS_ALIVE)),"/isAlive");
		handlerList.addHandler(contextSearch);
		
		/*
		 * 그룹핑 전용검색.
		 * */
		final Context contextGroup = new Context(server, GROUP_CONTEXT, Context.SESSIONS);
		contextGroup.setMaxFormContentSize(10 * 1024 * 1024); //파라미터전송 10MB까지 가능.
		contextGroup.addServlet(new ServletHolder(new GroupSearchServlet(WebServiceHttpServlet.JSON_TYPE)),"/json");
		contextGroup.addServlet(new ServletHolder(new GroupSearchServlet(WebServiceHttpServlet.JSONP_TYPE)),"/jsonp");
		contextGroup.addServlet(new ServletHolder(new GroupSearchServlet(WebServiceHttpServlet.XML_TYPE)),"/xml");
		handlerList.addHandler(contextGroup);
		
		
		final Context contextExecute = new Context(server, EXECUTE_CONTEXT, Context.SESSIONS);
		contextExecute.addServlet(new ServletHolder(new RegisterJobServlet(WebServiceHttpServlet.JSON_TYPE)),"/");
		handlerList.addHandler(contextExecute);
		
        // ServletContextHandler
		final Context contextKeywords = new Context(server, KEYWORD_CONTEXT, Context.SESSIONS);
		contextKeywords.addServlet(new ServletHolder(new RecommendKeywordServlet(WebServiceHttpServlet.JSON_TYPE)),"/recommend/json");
		contextKeywords.addServlet(new ServletHolder(new RecommendKeywordServlet(WebServiceHttpServlet.JSONP_TYPE)),"/recommend/jsonp");
		contextKeywords.addServlet(new ServletHolder(new RecommendKeywordServlet(WebServiceHttpServlet.XML_TYPE)),"/recommend/xml");
		contextKeywords.addServlet(new ServletHolder(new PopularKeywordServlet(WebServiceHttpServlet.JSON_TYPE)),"/popular/json");
		contextKeywords.addServlet(new ServletHolder(new PopularKeywordServlet(WebServiceHttpServlet.JSONP_TYPE)),"/popular/jsonp");
		contextKeywords.addServlet(new ServletHolder(new PopularKeywordServlet(WebServiceHttpServlet.XML_TYPE)),"/popular/xml");
		handlerList.addHandler(contextKeywords);

		// ANALYZER_TESTING_CONTEXT
		final Context contextAnalyzer = new Context(server, ANALYZER_CONTEXT, Context.SESSIONS);
		contextAnalyzer.addServlet(new ServletHolder(new AnalyzerServlet(WebServiceHttpServlet.JSON_TYPE)),"/");
		handlerList.addHandler(contextAnalyzer);
		
		// 구버전 TOKENIZER_TESTING_CONTEXT
		final Context contextTokenizer = new Context(server, TOKENIZER_CONTEXT, Context.SESSIONS);
		contextTokenizer.addServlet(new ServletHolder(new TokenizerServlet(WebServiceHttpServlet.JSON_TYPE)),"/");
		handlerList.addHandler(contextTokenizer);
		
        // DOCUMENT_LIST_CONTEXT
		final Context contextDocumentList = new Context(server, DOCUMENT_LIST_CONTEXT, Context.SESSIONS);
		contextDocumentList.addServlet(new ServletHolder(new DocumentListServlet(WebServiceHttpServlet.JSON_TYPE)),"/json");
		contextDocumentList.addServlet(new ServletHolder(new DocumentListServlet(WebServiceHttpServlet.XML_TYPE)),"/xml");
		handlerList.addHandler(contextDocumentList);
		
		// DOCUMENT_SEARCH_CONTEXT
		final Context contextDocumentSearch = new Context(server, DOCUMENT_SEARCH_CONTEXT, Context.SESSIONS);
		contextDocumentSearch.addServlet(new ServletHolder(new DocumentSearchServlet(WebServiceHttpServlet.JSON_TYPE)),"/json");
		contextDocumentSearch.addServlet(new ServletHolder(new DocumentSearchServlet(WebServiceHttpServlet.XML_TYPE)),"/xml");
		handlerList.addHandler(contextDocumentSearch);
		
		
		//7. 모니터링 MONITORING_CONTEXT
		final Context contextMonitoring = new Context(server, MONITORING_CONTEXT, Context.SESSIONS);
		//시스템정보
		contextMonitoring.addServlet(new ServletHolder(new ManagementInfoServlet(WebServiceHttpServlet.JSON_TYPE)),"/system");
		contextMonitoring.addServlet(new ServletHolder(new ManagementInfoServlet(WebServiceHttpServlet.JSONP_TYPE)),"/system/jsonp");
		
		contextMonitoring.addServlet(new ServletHolder(new SystemMonServlet(WebServiceHttpServlet.JSON_TYPE)),"/system/detail");
		contextMonitoring.addServlet(new ServletHolder(new SystemMonServlet(WebServiceHttpServlet.JSONP_TYPE)),"/system/detail/jsonp");
		
		//검색통계
		contextMonitoring.addServlet(new ServletHolder(new StatisticsInfoServlet(WebServiceHttpServlet.JSON_TYPE)),"/search");
		contextMonitoring.addServlet(new ServletHolder(new StatisticsInfoServlet(WebServiceHttpServlet.JSONP_TYPE)),"/search/jsonp");
		
		contextMonitoring.addServlet(new ServletHolder(new SearchMonServlet(WebServiceHttpServlet.JSON_TYPE)),"/search/detail");
		contextMonitoring.addServlet(new ServletHolder(new SearchMonServlet(WebServiceHttpServlet.JSONP_TYPE)),"/search/detail/jsonp");
		
		//검색엔진정보
		contextMonitoring.addServlet(new ServletHolder(new IndexingInfoServlet(WebServiceHttpServlet.JSON_TYPE)),"/indexing");
		contextMonitoring.addServlet(new ServletHolder(new IndexingInfoServlet(WebServiceHttpServlet.JSONP_TYPE)),"/indexing/jsonp");
		//실시간 검색키워드 SearchKeywordListServlet
		contextMonitoring.addServlet(new ServletHolder(new SearchKeywordListServlet(WebServiceHttpServlet.JSON_TYPE)),"/keywordList");
		contextMonitoring.addServlet(new ServletHolder(new SearchKeywordListServlet(WebServiceHttpServlet.JSONP_TYPE)),"/keywordList/jsonp");
		//이벤트 내역
		contextMonitoring.addServlet(new ServletHolder(new SearchEventListServlet()),"/eventList");
		//csv파일
		contextMonitoring.addServlet(new ServletHolder(new CSVMakeServlet()),"/csv");
		handlerList.addHandler(contextMonitoring);
		
		
		final Context contextConsole = new Context(server, CONSOLE_CONTEXT, Context.SESSIONS);
		contextConsole.addServlet(new ServletHolder(new ConsoleActionServlet()), "/command");
		handlerList.addHandler(contextConsole);
		
		for(Handler handler : handlerList.getHandlers()) {
			if(handler instanceof Context) {
				((Context)handler).setClassLoader(this.getClass().getClassLoader());
			}
		}
		
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
