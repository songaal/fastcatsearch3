package org.fastcatsearch.servlet;

import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.JobService;

public class JobHttpServlet extends WebServiceHttpServlet {

	private static final long serialVersionUID = -6799888063493417231L;
	
	
	public JobHttpServlet(int resultType){
		super(resultType);
    }
	
	public void init(){
		super.init();
	}
	
//	protected JobExecutor getJobExecutor() {
//		return jobExecutor;
//	}
}
