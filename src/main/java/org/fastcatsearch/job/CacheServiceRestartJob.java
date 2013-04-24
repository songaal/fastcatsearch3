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


import org.fastcatsearch.service.IRService;
import org.fastcatsearch.service.ServiceException;

public class CacheServiceRestartJob extends Job{
	private int delay;
	
	public CacheServiceRestartJob(){ }
	
	public CacheServiceRestartJob(int delay){ 
		this.delay = delay;
	}
	
	@Override
	public JobResult doRun() {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) { }
		
		boolean result = IRService.getInstance().searchCache().unload()
		&& IRService.getInstance().groupingCache().unload()
		&& IRService.getInstance().documentCache().unload()
		&& IRService.getInstance().searchCache().load()
		&& IRService.getInstance().groupingCache().load()
		&& IRService.getInstance().documentCache().load();
		
		return new JobResult(result);
	}

}
