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

import org.fastcatsearch.control.JobException;
import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class Job implements Runnable, Serializable{
	private static final long serialVersionUID = 6296052043270199612L;

	protected static Logger logger = LoggerFactory.getLogger(Job.class);
	protected Environment environment;
	protected JobExecutor jobExecutor;
	
	protected long jobId = -1;
	protected Object args;
	protected boolean isScheduled;
	protected boolean noResult; //결과가 필요없는 단순 호출 작업
	
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
		return "[Job] jobId = "+jobId+", args = "+args+", isScheduled="+isScheduled+", noResult="+noResult;
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
	
	public abstract JobResult doRun() throws JobException, ServiceException;

	public final void run(){
		long st = System.currentTimeMillis();
		try {
			JobResult jobResult = doRun();
			
			if(jobExecutor == null){
				throw new JobException("결과를 반환할 jobExecutor가 없습니다.");
			}
			
			if(jobId != -1){
				logger.debug("Job#{} result = {}", jobId, jobResult);
				jobExecutor.result(jobId, this, jobResult.result, jobResult.isSuccess, st, System.currentTimeMillis());
				
				Object result = jobResult.result;
				if(result != null && result instanceof Throwable){
					Throwable e = (Throwable) result;
					logError(e);
				}
			}else{
				logger.error("## 결과에 jobId가 없습니다. job={}, result=", this, jobResult);
			}
			
		} catch (OutOfMemoryError e){
			jobExecutor.result(jobId, this, e, false, st, System.currentTimeMillis());
			EventDBLogger.error(EventDBLogger.CATE_MANAGEMENT, "메모리부족 에러가 발생했습니다.", EventDBLogger.getStackTrace(e));
			logError(e);
		} catch (Throwable e){
			jobExecutor.result(jobId, this, e, false, st, System.currentTimeMillis());
			logError(e);
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
