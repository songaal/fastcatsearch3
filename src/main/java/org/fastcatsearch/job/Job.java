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

import org.fastcatsearch.control.JobController;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.log.EventDBLogger;
import org.fastcatsearch.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class Job implements Runnable, Serializable{
	private static final long serialVersionUID = 6296052043270199612L;

	protected static Logger logger = LoggerFactory.getLogger(Job.class);
	
	protected long jobId = -1;
	protected Object args;
	protected boolean isScheduled;
	protected boolean noResult; //결과가 필요없는 단순 호출 작업
	
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
//			if(jobId != -1 && !noResult){
			if(jobId != -1){
				logger.debug("Job_{} result = {}", jobId, result);
				JobController.getInstance().result(jobId, this, result, true, st, System.currentTimeMillis());
			}
			
		} catch (JobException e){
			JobController.getInstance().result(jobId, this, e.getMessage(), false, st, System.currentTimeMillis());
			StringWriter w = new StringWriter();
			e.printStackTrace(new PrintWriter(w));
			logger.error("#############################################\n"+w.toString()+"\\n#############################################");
		} catch (OutOfMemoryError e){
			JobController.getInstance().result(jobId, this, e.getMessage(), false, st, System.currentTimeMillis());
			EventDBLogger.error(EventDBLogger.CATE_MANAGEMENT, "메모리부족 에러가 발생했습니다.", EventDBLogger.getStackTrace(e));
			StringWriter w = new StringWriter();
			e.printStackTrace();
			e.printStackTrace(new PrintWriter(w));
			logger.error("#############################################\n"+w.toString()+"\n#############################################");
		} catch (Exception e){
			JobController.getInstance().result(jobId, this, e.getMessage(), false, st, System.currentTimeMillis());
			StringWriter w = new StringWriter();
			e.printStackTrace(new PrintWriter(w));
			logger.error("#############################################\n"+w.toString()+"\n#############################################");
		} catch(Error err){
			JobController.getInstance().result(jobId, this, err.getMessage(), false, st, System.currentTimeMillis());
			StringWriter w = new StringWriter();
			err.printStackTrace(new PrintWriter(w));
			logger.error("#############################################\n"+w.toString()+"\n#############################################");
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
	
}
