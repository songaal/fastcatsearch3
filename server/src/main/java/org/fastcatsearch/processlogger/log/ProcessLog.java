package org.fastcatsearch.processlogger.log;

import org.fastcatsearch.common.io.Streamable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface ProcessLog extends Streamable {
	public static final Logger logger = LoggerFactory.getLogger(ProcessLog.class);
}
