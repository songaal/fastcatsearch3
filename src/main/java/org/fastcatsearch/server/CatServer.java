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

package org.fastcatsearch.server;

import java.io.File;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.db.DBService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.KeywordService;
import org.fastcatsearch.service.QueryCacheService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.service.WebService;
import org.fastcatsearch.statistics.StatisticsInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CatServer {
	
private ServiceManager serviceManager;
	
	public static long startTime;
	public static CatServer instance;
	private static Logger logger;
	private boolean isRunning;
	
	public static void main(String... args) throws ServiceException{
		CatServer server = new CatServer();
		server.init(args);
		server.start();
	}
	
	public boolean isRunning(){
		return isRunning;
	}
	
	public static CatServer getInstance(){
		return instance;
	}
	
	public CatServer(){ }
		
	public void init(String[] args) throws ServiceException{
		//검색엔진으로 전달되는 args를 받아서 셋팅해준다.
		//대부분 -D옵션을 통해 전달받으므로 아직까지는 셋팅할 내용은 없다.
	}
	
	public boolean start() throws ServiceException{
		//초기화 및 서비스시작을 start로 옮김.
		//초기화로직이 init에 존재할 경우, 관리도구에서 검색엔진을 재시작할때, init을 호출하지 않으므로, 초기화를 건너뛰게 됨.
		
		instance = this;
		String ServerHome = System.getProperty("server.home");
		if(ServerHome == null){
			System.err.println("Warning! Please set env variable \"server.home\".");
			System.out.println("Usage : java com.fastcatsearch.server.CatServer -Dserver.home=[home path]");
			System.exit(1);
		}		
		
		File f = new File(ServerHome);
		if(!f.exists()){
			System.err.println("Warning! Path \""+ServerHome+"\" is not exist!");
			System.out.println("Usage : java com.fastcatsearch.server.CatServer -Dserver.home=[home path]");
			System.exit(1);
		}
		
		IRSettings.setHome(ServerHome);
		Environment environment = new Environment(ServerHome).init();
//		Settings settings = environment.settingManager().getSettings();
		this.serviceManager = new ServiceManager(environment);
		serviceManager.asSingleton();
		
		DBService dbService = serviceManager.createService("db", DBService.class);
		dbService.asSingleton();
		KeywordService keywordService = serviceManager.createService("keyword", KeywordService.class);
		keywordService.asSingleton();
		JobService jobService = serviceManager.createService("job", JobService.class);
		jobService.asSingleton();
		IRService irService = serviceManager.createService("ir", IRService.class);
		irService.asSingleton();
		QueryCacheService queryCacheService = serviceManager.createService("query_cache", QueryCacheService.class);
		queryCacheService.asSingleton();
		StatisticsInfoService statisticsInfoService = serviceManager.createService("statistics_info", StatisticsInfoService.class);
		statisticsInfoService.asSingleton();
		NodeService nodeService = serviceManager.createService("node", NodeService.class);
		nodeService.asSingleton();
		
		WebService webService = serviceManager.createService("web", WebService.class);
		if(webService == null){
			throw new ServiceException("웹서비스를 초기화하지 못했습니다.");
		}
		webService.asSingleton();
		
		logger = LoggerFactory.getLogger(CatServer.class);
		
		try{
			dbService.start();
			jobService.start();
			nodeService.start();
			
			if(webService != null)
				webService.start();
			
			irService.start();
			statisticsInfoService.start();
			keywordService.start();
			queryCacheService.start();

		}catch(ServiceException e){
			logger.error("CatServer 시작에 실패했습니다.", e);
			stop();
			return false;
		}
		startTime = System.currentTimeMillis();
		
//		try {
//			boolean isValidLicense = LicenseSettings.getInstance().load();
//			if(isValidLicense){
//				logger.info("유효한 라이선스입니다. 기한 = {}", LicenseSettings.getInstance().getLicenseInfo().getDisplayExpiredDate());
//			}else{
//				logger.warn("라이선스가 유효하지 않습니다.");
//			}
//		} catch (LicenseException e1) {
//			logger.error("라이선스 에러.", e1);
//		}
		
		
		logger.info("CatServer started!");
		EventDBLogger.info(EventDBLogger.CATE_MANAGEMENT, "검색엔진이 시작했습니다.", "");
		isRunning = true;
		return true;
	}
	
	public boolean stop() throws ServiceException{
		
		//FIXME 뜨는 도중 에러 발생시 NullPointerException 발생가능성.
		serviceManager.getService(NodeService.class).stop();
		serviceManager.getService(StatisticsInfoService.class).stop();
		serviceManager.getService(KeywordService.class).stop();
		serviceManager.getService(IRService.class).stop();
		serviceManager.getService(WebService.class).stop();
		serviceManager.getService(JobService.class).stop();
		serviceManager.getService(DBService.class).stop();
		serviceManager.getService(QueryCacheService.class).stop();
		
		logger.info("CatServer shutdown!");
		EventDBLogger.info(EventDBLogger.CATE_MANAGEMENT, "검색엔진이 정지했습니다.", "");
		isRunning = false;
		return true;
	}
	
	public void close() throws ServiceException{
		serviceManager.getService(NodeService.class).close();
		serviceManager.getService(StatisticsInfoService.class).close();
		serviceManager.getService(KeywordService.class).close();
		serviceManager.getService(IRService.class).close();
		serviceManager.getService(WebService.class).close();
		serviceManager.getService(JobService.class).close();
		serviceManager.getService(DBService.class).close();
		serviceManager.getService(QueryCacheService.class).close();
	}
	
}
