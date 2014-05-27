package org.fastcatsearch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallStackTraceUtil {
	protected static Logger logger = LoggerFactory.getLogger(CallStackTraceUtil.class);
	public static void printStackTrace(){
		if(logger.isDebugEnabled()) {
			logger.debug("----------------------------------");
			for(StackTraceElement e : Thread.currentThread().getStackTrace()) {
				logger.debug(e.toString());
			}
			logger.debug("----------------------------------");
		}
	}
}
