package org.fastcatsearch.servlet;

import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.JobService;

public class JobHttpServlet extends WebServiceHttpServlet {

	private static final long serialVersionUID = -6799888063493417231L;
	
	private JobExecutor jobExecutor;
	
	public JobHttpServlet(int resultType, JobExecutor jobExecutor){
		super(resultType);
    	this.jobExecutor = jobExecutor;
    }
	
	public void init(){
		super.init();
		jobExecutor = JobService.getInstance();
	}
	
	protected JobExecutor getJobExecutor() {
		return jobExecutor;
	}
}
