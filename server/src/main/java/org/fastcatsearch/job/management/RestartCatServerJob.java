package org.fastcatsearch.job.management;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.server.CatServer;

/**
 * CatServer 엔진을 재시작한다. 
 */
public class RestartCatServerJob extends Job {

	private static final long serialVersionUID = 4892359499242325975L;

	public RestartCatServerJob(){
		//결과는 받지않는다.
		setNoResult();
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		CatServer.getInstance().restart();
		return null;
	}

}
