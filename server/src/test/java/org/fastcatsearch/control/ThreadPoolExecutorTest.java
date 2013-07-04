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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class ThreadPoolExecutorTest extends TestCase {
	public void test1(){
		int time = 2;
		int core = 99;
		int max = 100;
		
		BlockingQueue<Runnable> jobQueue = new LinkedBlockingQueue<Runnable>(10);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(core, max, 100, TimeUnit.SECONDS, jobQueue);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
		System.out.println("1 active = "+executor.getActiveCount());
		for (int i = 0; i < core+5; i++) {
			Runnable run1 = new TestRun(i+1,time);
			executor.execute(run1);
			System.out.println("2 active = "+executor.getActiveCount());
		}
		
		System.out.println("#max = "+executor.getMaximumPoolSize());
		System.out.println("#core = "+executor.getCorePoolSize());
		System.out.println("#active = "+executor.getActiveCount());
		
//		while(executor.getActiveCount() > 0){
		for (int i = 0; i < 10; i++) {
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("3 active = "+executor.getActiveCount());
		}
		
	}
}
class TestRun implements Runnable{

	int time;
	int seq;
	public TestRun(int seq, int time){
		this.seq = seq;
		this.time = time;
	}
	public void run() {
		System.out.println("start "+seq);
		try {
			Thread.sleep(time * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("end "+seq);
	}
	
	
}


