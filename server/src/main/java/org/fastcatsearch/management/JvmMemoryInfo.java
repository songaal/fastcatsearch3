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


public class JvmMemoryInfo {
	private static Logger logger = LoggerFactory.getLogger(JvmMemoryInfo.class);
	
	public int maxHeapMemory;
	public int committedHeapMemory;
	public int usedHeapMemory;
	public int maxNonHeapMemory;
	public int committedNonHeapMemory;
	public int usedNonHeapMemory;
	public int totalPhysicalMemorySize;
	
	public void init(int maxHeapMemory, int committedHeapMemory, int usedHeapMemory, int maxNonHeapMemory, int committedNonHeapMemory
			, int usedNonHeapMemory, int totalPhysicalMemorySize){
		this.maxHeapMemory = maxHeapMemory;
		this.committedHeapMemory = committedHeapMemory;
		this.usedHeapMemory = usedHeapMemory;
		this.maxNonHeapMemory = maxNonHeapMemory;
		this.committedNonHeapMemory = committedNonHeapMemory;
		this.usedNonHeapMemory = usedNonHeapMemory;
		this.totalPhysicalMemorySize = totalPhysicalMemorySize;
	}

	public void print() {
		logger.info("maxHeapMemory = {}, committedHeapMemory = {}, usedHeapMemory = {}, maxNonHeapMemory = {}, committedNonHeapMemory = {}" +
				", usedNonHeapMemory = {}, totalPhysicalMemorySize = {}", maxHeapMemory, committedHeapMemory, usedHeapMemory, maxNonHeapMemory, committedNonHeapMemory, usedNonHeapMemory, totalPhysicalMemorySize);
	
	}

}
