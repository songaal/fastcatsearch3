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

import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.JobException;
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
	
	public abstract Object run0() throws JobException, ServiceException;

	public final void run(){
		long st = System.currentTimeMillis();
		try {
			Object result = run0();
			
			if(jobExecutor == null){
				throw new JobException("결과를 반환할 jobExecutor가 없습니다.");
			}
			
			if(jobId != -1){
				logger.debug("Job#{} result = {}", jobId, result);
				if(result instanceof JobResult){
					JobResult r = (JobResult) result;
					Object ro = r.result;
					if(ro instanceof Throwable){
						Throwable e = (Throwable) ro;
						ro = e.getMessage();
						logError(e);
					}
					jobExecutor.result(jobId, this, ro, r.isSuccess, st, System.currentTimeMillis());
					
				}else{
					jobExecutor.result(jobId, this, result, true, st, System.currentTimeMillis());
				}
			}else{
				logger.error("## 결과에 jobId가 없습니다. job="+this+", result="+result);
			}
			
		} catch (OutOfMemoryError e){
			jobExecutor.result(jobId, this, e.getMessage(), false, st, System.currentTimeMillis());
			EventDBLogger.error(EventDBLogger.CATE_MANAGEMENT, "메모리부족 에러가 발생했습니다.", EventDBLogger.getStackTrace(e));
			logError(e);
		} catch (Throwable e){
			jobExecutor.result(jobId, this, e.getMessage(), false, st, System.currentTimeMillis());
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
	
	class JobResult{
		Object result;
		boolean isSuccess;
		public JobResult(Object result, boolean isSuccess){
			this.result = result;
			this.isSuccess = isSuccess;
		}
		public JobResult(ResultFuture resultFuture){
			result = resultFuture.get();
			isSuccess = resultFuture.isSuccess();
		}
		
	}
	
}
