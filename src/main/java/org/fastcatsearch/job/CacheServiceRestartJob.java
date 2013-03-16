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


import org.fastcatsearch.service.QueryCacheService;
import org.fastcatsearch.service.ServiceException;

public class CacheServiceRestartJob extends Job{
	private int delay;
	
	public CacheServiceRestartJob(){ }
	
	public CacheServiceRestartJob(int delay){ 
		this.delay = delay;
	}
	
	@Override
	public Object run0() {
		try {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) { }
			
			return QueryCacheService.getInstance().restart();
		} catch (ServiceException e) {
			return false;
		}
	}

}
