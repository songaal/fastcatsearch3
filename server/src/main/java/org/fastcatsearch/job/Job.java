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

package org.fastcatsearch.job;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

import org.fastcatsearch.alert.ClusterAlertService;
import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class Job implements Runnable, Serializable {
	private static final long serialVersionUID = 6296052043270199612L;

	protected static Logger logger = LoggerFactory.getLogger(Job.class);
	protected Environment environment;
	protected JobExecutor jobExecutor;
	
	protected long jobId = -1;
	protected Object args;
	protected boolean isScheduled;
	protected boolean noResult; //결과가 필요없는 단순 호출 작업
	
	protected long startTime;
	protected long endTime;
	
	
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	public void setJobExecutor(JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
	}
	public JobExecutor getJobExecutor() {
		return jobExecutor;
	}
	
	public String toString(){
		return "[Job] jobId = "+jobId+", "+getClass().getSimpleName()+", args = "+args+", isScheduled="+isScheduled+", noResult="+noResult;
//		return "[Job] jobId = "+jobId+", args = "+args+", isScheduled="+isScheduled;
	}
	public void setId(long jobId) {
		this.jobId = jobId;
	}

	public long getId(){
		return jobId;
	}
	
	public void setArgs(Object args){
		this.args = args;
	}
	
	public Object getArgs(){
		return args;
	}
	public String getStringArgs(int i){
		return ((String[])args)[i];
	}
	public String getStringArgs(){
		return (String)args;
	}
	public String[] getStringArrayArgs(){
		return (String[])args;
	}
	public void setScheduled(boolean isScheduled) {
		this.isScheduled = isScheduled;
	}

	public boolean isScheduled(){
		return isScheduled;
	}
	
	public void setNoResult(){
		noResult = true; 
	}
	
	public boolean isNoResult(){
		return noResult;
	}
	
	public long jobStartTime(){
		return startTime;
	}
	
	public long jobEndTime(){
		return endTime;
	}
	
	public long duration(){
		return endTime - startTime;
	}
	public abstract JobResult doRun() throws FastcatSearchException;

	static AtomicInteger count = new AtomicInteger();
	
	public final void run() {
		startTime = System.currentTimeMillis();
		JobResult jobResult = null;
		try {
			
			jobResult = doRun();
			
			if(jobId != -1){
				
//				logger.debug("Job#{} result = {}", jobId, jobResult);
				jobExecutor.result(this, jobResult.result, jobResult.isSuccess);
				
				Object result = jobResult.result;
				if(result != null && result instanceof Throwable){
					//다른 서버에서 발생한 에러의 경우는 예외가 result에 담겨있다.
					throw (Throwable) result;
				}
			}else{
				logger.error("## 결과에 jobId가 없습니다. job={}, result={}", this, jobResult);
				throw new FastcatSearchException("ERR-00110");
			}
		} catch (Throwable e){
			ClusterAlertService clusterAlertService = ServiceManager.getInstance().getService(ClusterAlertService.class);
			clusterAlertService.alert(e);
			jobExecutor.result(this, e, false);
		} finally {
			endTime = System.currentTimeMillis();
		}

	}
	
	protected void logError(Throwable e){
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		writer.println("#############################################");
		e.printStackTrace(writer);
		writer.println("#############################################");
		writer.close();
		logger.error(sw.toString());
	}
	
	public static class JobResult{
		protected Object result;
		protected boolean isSuccess;
		
		public JobResult(){
			//result를 넣지않으면 resultFuture.take()시 null객체가 리턴된다.
		}
		
		public JobResult(Object result){
			this.isSuccess = !(result instanceof Throwable);
			this.result = result;
		}
		
		public Object result(){
			return result;
		}
		
		public boolean isSuccess(){
			return isSuccess;
		}
		
		@Override
		public String toString(){
			return "[JobResult]success="+isSuccess+", result="+result;
		}
		
	}
	
}
