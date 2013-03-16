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


public class TestJob extends Job{

	public TestJob(){ }
	
	public TestJob(String str){
		args = new String[]{str};
	}
	
	@Override
	public Object run0() {
		String[] args = getStringArrayArgs();
		String str = args[0];

		logger.debug("This is Test Job!! args="+str);
		
		return str;
	}

}
