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

package org.fastcatsearch.control;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class JobResult {
	
	private BlockingQueue queue = new LinkedBlockingQueue();
	private boolean isSuccess;
	
	public void put(Object result, boolean isSuccess) throws InterruptedException{
		this.isSuccess = isSuccess;
		queue.put(result);
	}
	
	public boolean isSuccess(){
		return isSuccess;
	}
	
	public Object take() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			return null;
		}
	}
	
	public Object poll(int time) {
		try {
			return queue.poll(time, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return null;
		}
	}
}
