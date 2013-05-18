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

import java.util.Map;

import org.fastcatsearch.exception.FastcatSearchException;




public class MonitorJobExecutorStreamableJob extends MonitorJobExecutorJob {

	public MonitorJobExecutorStreamableJob(){ }
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		JobResult jobResult = super.doRun();
		if(jobResult.isSuccess()){
			Map<String, String> map = (Map<String, String>) jobResult.result();
			return new JobResult(map);
		}
		return null;
	}
	
}
