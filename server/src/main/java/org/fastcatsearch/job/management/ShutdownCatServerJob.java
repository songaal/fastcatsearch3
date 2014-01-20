package org.fastcatsearch.job.management;

import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.Job;

/**
 * CatServer 엔진을 shutdown한다. 
 */
public class ShutdownCatServerJob extends Job {

	private static final long serialVersionUID = 4892359499242325975L;

	public ShutdownCatServerJob(){
		//결과는 받지않는다.
		setNoResult();
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		//수행이 보장되야하므로, non-daemon thread로 생성.
		Thread t = new Thread("shutdown thread"){
			public void run() {
				System.exit(0);
			}
		};
		t.setDaemon(false);
		t.start();
		return null;
	}

}
