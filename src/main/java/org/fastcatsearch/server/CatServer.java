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

import org.fastcatsearch.control.JobController;
import org.fastcatsearch.db.DBHandler;
import org.fastcatsearch.ir.config.IRConfig;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.KeywordService;
import org.fastcatsearch.service.QueryCacheService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceHandler;
import org.fastcatsearch.statistics.StatisticsInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CatServer {
	
	private ServiceHandler serviceHandler;
	private IRService irService;
	private JobController jobController;
	private DBHandler dbHandler;
	private KeywordService keywordService;
	private PreparedLoader preparedLoader;
	private QueryCacheService cacheService;
	private StatisticsInfoService statisticsInfoService;
	
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
			System.out.println("Usage : java org.fastcatsearch.server.CatServer -Dserver.home=[home path]");
			System.exit(1);
		}		
		
		File f = new File(ServerHome);
		if(!f.exists()){
			System.err.println("Warning! Path \""+ServerHome+"\" is not exist!");
			System.out.println("Usage : java org.fastcatsearch.server.CatServer -Dserver.home=[home path]");
			System.exit(1);
		}

		IRSettings.setHome(ServerHome);
		dbHandler = DBHandler.getInstance();
		keywordService = KeywordService.getInstance();
		jobController = JobController.getInstance();
		irService = IRService.getInstance();
		cacheService = QueryCacheService.getInstance();
		preparedLoader = new PreparedLoader();
		statisticsInfoService = StatisticsInfoService.getInstance();
		
		try{
			serviceHandler = ServiceHandler.getInstance();
		}catch(Exception e){
			throw new ServiceException("서비스를 초기화하지 못했습니다.",e);
		}
		
		logger = LoggerFactory.getLogger(CatServer.class);
		
		IRConfig irConfig = IRSettings.getConfig(true);
		boolean statisticsServiceStart = irConfig.getBoolean("statistics.service.start");
		try{
			dbHandler.start();
			jobController.start();
			
			preparedLoader.load();
			
			if(serviceHandler != null)
				serviceHandler.start();
			
			irService.start();
			if(statisticsServiceStart){
				statisticsInfoService.start();
			}
			keywordService.start();
			cacheService.start();
		}catch(ServiceException e){
			logger.error("CatServer 시작에 실패했습니다.", e);
			stop();
			return false;
		}
		startTime = System.currentTimeMillis();
		logger.info("CatServer started!");
		EventDBLogger.info(EventDBLogger.CATE_MANAGEMENT, "검색엔진이 시작했습니다.", "");
		isRunning = true;
		return true;
	}
	
	public boolean stop() throws ServiceException{
		preparedLoader.unload();
		statisticsInfoService.shutdown();
		keywordService.shutdown();
		irService.shutdown();
		if(serviceHandler != null)
			serviceHandler.shutdown();
		jobController.shutdown();
		dbHandler.shutdown();
		cacheService.shutdown();
		logger.info("CatServer shutdown!");
		EventDBLogger.info(EventDBLogger.CATE_MANAGEMENT, "검색엔진이 정지했습니다.", "");
		isRunning = false;
		return true;
	}
	
	public void destroy() throws ServiceException{
		dbHandler = null;
		irService = null;
		serviceHandler = null;
		jobController = null;
		keywordService = null;
		preparedLoader = null;
		cacheService = null;
		statisticsInfoService = null;
	}
	
}
