package org.fastcatsearch.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemDiskInfo {
	private static Logger logger = LoggerFactory.getLogger(SystemDiskInfo.class);
	
	public int totalDiskSize;
	public int usedDiskSize;
	public int freeDiskSize;
	
	public SystemDiskInfo(){
	}

	public void print() {
		logger.info("totalDiskSize = {}, usedDiskSize = {}, freeDiskSize = {}", totalDiskSize, usedDiskSize, freeDiskSize);
	}
	
}
