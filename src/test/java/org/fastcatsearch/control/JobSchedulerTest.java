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

import java.sql.Timestamp;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.ir.config.IRSettings;
import org.fastcatsearch.ir.config.SettingException;
import org.fastcatsearch.service.ServiceException;

import junit.framework.TestCase;


public class JobSchedulerTest extends TestCase{
	public void test1() throws SettingException, ServiceException{
		IRSettings.setHome("\\fastcat");
		JobService c = JobService.getInstance();
		c.start();
		c.setSchedule("addJob","org.fastcatsearch.ir.job.AddJob", "8 5", Timestamp.valueOf("2010-10-08 11:42"), 6000, true);
		try {
			Thread.sleep(1000*60);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
