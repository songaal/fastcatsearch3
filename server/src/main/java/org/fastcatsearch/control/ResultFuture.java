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

import org.fastcatsearch.error.SearchError;
import org.fastcatsearch.error.ServerErrorCode;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.indexing.IndexingJob;
import org.fastcatsearch.job.internal.InternalDocumentSearchJob;
import org.fastcatsearch.job.internal.InternalSearchJob;
import org.fastcatsearch.job.search.ClusterSearchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ResultFuture {
	private static Logger logger = LoggerFactory.getLogger(ResultFuture.class);
	protected BlockingQueue<Object> queue;
	protected boolean isSuccess;
	protected long requestId;
	protected Map<Long, ? extends ResultFuture> resultFutureMap;
	protected long startTime;
	protected Object result;
	private static NullResult NULL_RESULT = new NullResult();
	private Job job;
	private static class NullResult { }
	
	/**
	 * 실패 결과.
	 * */
	public ResultFuture() {
		requestId = -1;
		result = NULL_RESULT;
	}

    public ResultFuture(long requestId, Map<Long, ? extends ResultFuture> resultFutureMap) {
        this(requestId, resultFutureMap, null);
    }
	public ResultFuture(long requestId, Map<Long, ? extends ResultFuture> resultFutureMap, Job job) {
		this.requestId = requestId;
		this.resultFutureMap = resultFutureMap;
		this.startTime = System.currentTimeMillis();
		queue = new LinkedBlockingQueue<Object>();
        this.job = job;
	}

	public long getElapsedTimeMilis(){
		return System.currentTimeMillis() - startTime;
	}
	public void putNullFail() {
		this.isSuccess = false;
		try {
			queue.put(NULL_RESULT);
		} catch (InterruptedException ignore) { }
	}
	
	public void put(Object result, boolean isSuccess) {
		this.isSuccess = isSuccess;
		try {
			if(result == null){
				queue.put(NULL_RESULT);
			}else{
				queue.put(result);
			}
		} catch (InterruptedException ignore) { }
	}
	
	public boolean isSuccess(){
		return isSuccess;
	}
	
	public Object get(){
		return result;
	}
	
	public Object take() {
		if(result != null){
			if(result == NULL_RESULT){
				return null;
			}else{
				return result;
			}
		}
		
		try {
            long timeout = 0;
			if(job != null) {
				timeout = job.getTimeout();
			}

            if(timeout > 0) {
                result = pollInMillis(timeout);
            } else {
                result = queue.take();
            }
//			result = queue.take();
			if(result == NULL_RESULT){
				return null;
			}
			return result;
		} catch (InterruptedException e) {
			//결과를 받지 못할경우, map에서 제거해준다.
			resultFutureMap.remove(requestId);
			return null;
		}
	}

    public Object poll(int timeInSecond) {
        return pollInMillis(timeInSecond * 1000);
    }

	public Object pollInMillis(long time) {
		if(result != null){
			if(result == NULL_RESULT){
				return null;
			}else{
				return result;
			}
		}
		long remainMilisecondTime = time - (System.currentTimeMillis() - startTime);
		try {
			if(remainMilisecondTime > 0){
				Object result = queue.poll(remainMilisecondTime, TimeUnit.MILLISECONDS);
				if(result == null){
                    if(job != null && job.isForceAbortWhenTimeout()) {
                        job.abortJob();
                    }
                    if(job != null && (job instanceof ClusterSearchJob || job instanceof InternalSearchJob || job instanceof InternalDocumentSearchJob)) {
						//결과가 아직도착하지 않아서 받지못하거나, 네트워크 문제로 인해 전달이 안될수도 있으므로 불필요한 객체를 map에서 제거한다.
						resultFutureMap.remove(requestId);
						result = new SearchError(ServerErrorCode.SEARCH_TIMEOUT_ERROR, String.valueOf(time));
					} else {
                        result = new SearchError(ServerErrorCode.JOB_TIMEOUT_ERROR, String.valueOf(time));
                    }
				}else if(result == NULL_RESULT){
					return null;
				}
				return result;
			}else{
				Object result = queue.poll();
				if(result == null){
					//시간초과에 따른 제거일수도 있으므로, 
					resultFutureMap.remove(requestId);
                    if(job != null && job.isForceAbortWhenTimeout()) {
                        job.abortJob();
                    }
				}else if(result == NULL_RESULT){
					return null;
				}
				return result;
			}
		} catch (InterruptedException e) {
			resultFutureMap.remove(requestId);
			return null;
		}
	}
	
	
}
