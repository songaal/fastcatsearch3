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

import org.fastcatsearch.common.Lifecycle;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractService {
	protected static Logger logger = LoggerFactory.getLogger(AbstractService.class);
	
	private static AbstractService instance;
	
	protected Lifecycle lifecycle;
	protected Environment environment;
	protected Settings settings;
	
	public AbstractService(Environment environment, Settings settings){
		this.environment = environment;
		this.settings = settings;
		lifecycle = new Lifecycle();
	}
	
	
	public void asSingleton(){
		instance = this;
	}
	
	public static <T extends AbstractService> T getInstance(){
		return (T) instance;
	}
	
	public boolean isRunning() throws ServiceException{
		return lifecycle.started();
	}
	
	public boolean start() throws ServiceException{
		if(lifecycle.canMoveToStarted()){
			if(doStart()){
				logger.info(getClass().getSimpleName()+" 시작!");
				EventDBLogger.info(EventDBLogger.CATE_MANAGEMENT, getClass().getSimpleName()+"가 시작했습니다.", "");
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	protected abstract boolean doStart() throws ServiceException;
		
	public boolean stop() throws ServiceException{
		if(lifecycle.canMoveToStopped()){
			if(doStop()){
				logger.info(getClass().getSimpleName()+" 정지!");
				EventDBLogger.info(EventDBLogger.CATE_MANAGEMENT, getClass().getSimpleName()+"가 정지했습니다.", "");
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	protected abstract boolean doStop() throws ServiceException;
	
	public boolean restart() throws ServiceException{
		logger.info(this.getClass().getName()+" restart..");
		if(stop()){
		//start는 성공해야 하므로 해당값을 리턴해준다.
			return start();
		}
		return false;
	}
	
	public boolean close() throws ServiceException{
		if(lifecycle.canMoveToClosed()){
			if(doClose()){
				logger.info(getClass().getSimpleName()+" 정지!");
				EventDBLogger.info(EventDBLogger.CATE_MANAGEMENT, getClass().getSimpleName()+"가 정지했습니다.", "");
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	protected abstract boolean doClose() throws ServiceException;
	
}
