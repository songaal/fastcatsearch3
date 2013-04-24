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
	
	public void init(int maxHeapMemory, int committedHeapMemory, int usedHeapMemory, int maxNonHeapMemory, int committedNonHeapMemory, int usedNonHeapMemory){
		this.maxHeapMemory = maxHeapMemory;
		this.committedHeapMemory = committedHeapMemory;
		this.usedHeapMemory = usedHeapMemory;
		this.maxNonHeapMemory = maxNonHeapMemory;
		this.committedNonHeapMemory = committedNonHeapMemory;
		this.usedNonHeapMemory = usedNonHeapMemory;
	}

	public void print() {
		logger.trace("maxHeapMemory = {}, committedHeapMemory = {}, usedHeapMemory = {}, maxNonHeapMemory = {}, committedNonHeapMemory = {}" +
				", usedNonHeapMemory = {}", new Object[]{maxHeapMemory, committedHeapMemory, usedHeapMemory, maxNonHeapMemory, committedNonHeapMemory, usedNonHeapMemory});
	
	}

	public void add(JvmMemoryInfo jvmMemoryInfoPerSecond) {
		
		if(jvmMemoryInfoPerSecond.maxHeapMemory > maxHeapMemory)
			maxHeapMemory = jvmMemoryInfoPerSecond.maxHeapMemory;
		
		committedHeapMemory += jvmMemoryInfoPerSecond.committedHeapMemory;
		usedHeapMemory += jvmMemoryInfoPerSecond.usedHeapMemory;
		
		if(jvmMemoryInfoPerSecond.maxNonHeapMemory > maxNonHeapMemory)
			maxNonHeapMemory = jvmMemoryInfoPerSecond.maxNonHeapMemory;
		
		committedNonHeapMemory += jvmMemoryInfoPerSecond.committedNonHeapMemory;
		usedNonHeapMemory += jvmMemoryInfoPerSecond.usedNonHeapMemory;
	}

	public JvmMemoryInfo getAverage(int countPerMinute) {
		JvmMemoryInfo memoryInfo = new JvmMemoryInfo();
		
		memoryInfo.maxHeapMemory = maxHeapMemory;
		memoryInfo.committedHeapMemory = committedHeapMemory / countPerMinute;
		memoryInfo.usedHeapMemory = usedHeapMemory / countPerMinute;
		
		memoryInfo.maxNonHeapMemory = maxNonHeapMemory;
		memoryInfo.committedNonHeapMemory = committedNonHeapMemory / countPerMinute;
		memoryInfo.usedNonHeapMemory = usedNonHeapMemory / countPerMinute;
		
		maxHeapMemory = 0;
		committedHeapMemory = 0;
		usedHeapMemory = 0;
		maxNonHeapMemory = 0;
		committedNonHeapMemory = 0;
		usedNonHeapMemory = 0;
		return memoryInfo;
	}
	
}
