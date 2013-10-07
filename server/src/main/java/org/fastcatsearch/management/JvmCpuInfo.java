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

package org.fastcatsearch.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JvmCpuInfo {
	private static Logger logger = LoggerFactory.getLogger(JvmCpuInfo.class);
	public int jvmCpuUse;
	public int systemCpuUse;
	public double systemLoadAverage;
	
	public void print() {
		logger.info("jvmCpuUse = {}, systemCpuUse={}, systemLoadAverage={}", jvmCpuUse, systemCpuUse, systemLoadAverage);
	}
}
