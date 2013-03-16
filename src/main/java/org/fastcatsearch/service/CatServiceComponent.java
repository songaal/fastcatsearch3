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

import org.fastcatsearch.log.EventDBLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class CatServiceComponent {
	protected static Logger logger = LoggerFactory.getLogger(CatServiceComponent.class);
	
	protected boolean isRunning;
	
	public boolean isRunning() throws ServiceException{
		return isRunning;
	}
	
	public boolean start() throws ServiceException{
		if(isRunning){
			logger.error("이미 실행중입니다.");
			return false;
		}
		if(start0()){
			isRunning = true;
			logger.info(getClass().getSimpleName()+" 시작!");
			EventDBLogger.info(EventDBLogger.CATE_MANAGEMENT, getClass().getSimpleName()+"가 시작했습니다.", "");
			return true;
		}else{
			return false;
		}
	}
	
	protected abstract boolean start0() throws ServiceException;
		
	public boolean shutdown() throws ServiceException{
		if(!isRunning){
			logger.error(getClass().getSimpleName()+"가 시작중이 아닙니다.");
			return false;
		}
		if(shutdown0()){
			isRunning = false;
			logger.info(getClass().getSimpleName()+" 정지!");
			EventDBLogger.info(EventDBLogger.CATE_MANAGEMENT, getClass().getSimpleName()+"가 정지했습니다.", "");
			return true;
		}else{
			return false;
		}
	}
	
	protected abstract boolean shutdown0() throws ServiceException;
		
	public boolean restart() throws ServiceException{
		logger.info(this.getClass().getName()+" restart..");
		//시작중이든 정지중이든 restart는 무조건 정지명령을 내린다.
		shutdown0();
		//start는 성공해야 하므로 해당값을 리턴해준다.
		return start0();
	}
	
}
