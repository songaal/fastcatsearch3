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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.fastcatsearch.alert.ClusterAlertService;
import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.error.SearchAbortError;
import org.fastcatsearch.error.SearchError;
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
	protected boolean noResult; // 결과가 필요없는 단순 호출 작업

	private long startTime;
	private long endTime;

    private long timeout; //타임아웃.
    private boolean forceAbortWhenTimeout; //timeout 이 지나면 강제로 abort시킨다. abort프로세스는 job에서 구현필요.

    protected boolean isAborted;

    public void abortJob() {
        logger.debug("Request abort job > {}", this);
        isAborted = true;
        whenAborted();
    }

    protected void whenAborted() {
        //필요한 job에서만 구현한다.
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout, boolean forceAbortWhenTimeout) {
        this.timeout = timeout;
        this.forceAbortWhenTimeout = forceAbortWhenTimeout;
    }

    public boolean isForceAbortWhenTimeout() {
        return forceAbortWhenTimeout;
    }

    public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public void setJobExecutor(JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
	}

	public JobExecutor getJobExecutor() {
		return jobExecutor;
	}

	public String toString() {
		return "[Job] jobId = " + jobId + ", " + getClass().getSimpleName() + ", args = " + args + ", isScheduled=" + isScheduled + ", noResult=" + noResult;
	}

	public void setId(long jobId) {
		this.jobId = jobId;
	}

	public long getId() {
		return jobId;
	}

	public void setArgs(Object args) {
		this.args = args;
	}

	public Object getArgs() {
		return args;
	}

	public String getStringArgs(int i) {
		return ((String[]) args)[i];
	}

	public String getStringArgs() {
		return (String) args;
	}

	public String[] getStringArrayArgs() {
		return (String[]) args;
	}

	public Map<String, Object> getMapArgs() {
		return (Map<String, Object>) args;
	}

	public void setScheduled(boolean isScheduled) {
		this.isScheduled = isScheduled;
	}

	public boolean isScheduled() {
		return isScheduled;
	}

	public void setNoResult() {
		noResult = true;
	}

	public boolean isNoResult() {
		return noResult;
	}

	public long jobStartTime() {
		return startTime;
	}

	public long jobEndTime() {
		return endTime;
	}

	public long duration() {
		return endTime - startTime;
	}

	public abstract JobResult doRun() throws FastcatSearchException;

	static AtomicInteger count = new AtomicInteger();

	public final void run() {
		startTime = System.currentTimeMillis();
		Object result = null;
		boolean isSuccess = false;
		try {
			if (this instanceof MasterNodeJob) {
				if (!environment.isMasterNode()) {
					// 실행하면 안된다.
					throw new RuntimeException("Cannot execute MasterNodeJob on other node : " + environment.myNodeId());
				}
			}
			
			JobResult jobResult = doRun();
			
			if(jobResult != null) {
				result = jobResult.result;
				isSuccess = jobResult.isSuccess;
				if (result != null && result instanceof Throwable) {
					// 다른 서버에서 발생한 에러의 경우는 예외가 result에 담겨있다.
//					throw (Throwable) result;
				}
			}
			
			if (jobId == -1) {
				logger.error("## 결과에 jobId가 없습니다. job={}, result={}", this, jobResult);
				throw new FastcatSearchException("ERR-00110");
			}
        } catch (SearchAbortError e) {
            result = e;
            isSuccess = false;
            logger.debug("search aborted by timeout " + getClass().getName() + " " + (args != null ? args : ""));
        } catch (SearchError e) {
            //검색에러는 따로 시스템에러 처리하지 않는다.
            result = e;
            isSuccess = false;
		} catch (Throwable e) {
			result = e;
			isSuccess = false;
			logger.error("error at " + getClass().getName() + " " + args, e);
			ClusterAlertService clusterAlertService = ServiceManager.getInstance().getService(ClusterAlertService.class);
			clusterAlertService.alert(e);
		} finally {
			//결과셋팅.
			jobExecutor.result(this, result, isSuccess);
			endTime = System.currentTimeMillis();
		}

	}

	protected void logError(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		writer.println("#############################################");
		e.printStackTrace(writer);
		writer.println("#############################################");
		writer.close();
		logger.error(sw.toString());
	}

	public static class JobResult {
		protected Object result;
		protected boolean isSuccess;

		public JobResult() {
			// result를 넣지않으면 resultFuture.take()시 null객체가 리턴된다.
		}

		public JobResult(Object result) {
			this.isSuccess = !(result instanceof Throwable);
			this.result = result;
		}

		public Object result() {
			return result;
		}

		public boolean isSuccess() {
			return isSuccess;
		}

		@Override
		public String toString() {
			return "[JobResult]success=" + isSuccess + ", result=" + result;
		}

	}

}
