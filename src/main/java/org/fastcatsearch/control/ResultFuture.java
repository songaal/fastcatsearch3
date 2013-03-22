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

package org.fastcatsearch.control;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ResultFuture {
	protected BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	protected boolean isSuccess;
	protected long requestId;
	protected Map<Long, ? extends ResultFuture> resultFutureMap;
	protected long startTime;
	protected Object result;
	
	public ResultFuture(long requestId, Map<Long, ? extends ResultFuture> resultFutureMap) {
		this.requestId = requestId;
		this.resultFutureMap = resultFutureMap;
		this.startTime = System.currentTimeMillis();
	}

	public void put(Object result, boolean isSuccess) {
		this.isSuccess = isSuccess;
		try {
			queue.put(result);
		} catch (InterruptedException e) {
			//ignore
		}
	}
	
	public boolean isSuccess(){
		return isSuccess;
	}
	
	public Object get(){
		return result;
	}
	
	public Object take() {
		if(result != null){
			return result;
		}
		
		try {
			result = queue.take();
			return result;
		} catch (InterruptedException e) {
			//결과를 받지 못할경우, map에서 제거해준다.
			resultFutureMap.remove(requestId);
			return null;
		}
	}
	
	public Object poll(int timeInSecond) {
		if(result != null){
			return result;
		}
		
		long remainTime = timeInSecond * 1000 - (System.currentTimeMillis() - startTime);
		try {
			if(remainTime > 0){
				Object result = queue.poll(remainTime, TimeUnit.SECONDS);
				if(result == null){
					//결과가 아직도착하지 않아서 받지못하거나, 네트워크 문제로 인해 전달이 안될수도 있으므로 불필요한 객체를 map에서 제거한다.
					resultFutureMap.remove(requestId);
				}
				return result;
			}else{
				Object result = queue.poll();
				if(result == null){
					resultFutureMap.remove(requestId);
				}
				return result;
			}
		} catch (InterruptedException e) {
			resultFutureMap.remove(requestId);
			return null;
		}
	}
}
