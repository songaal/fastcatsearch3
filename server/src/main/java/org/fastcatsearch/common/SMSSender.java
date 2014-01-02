package org.fastcatsearch.common;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SMSSender {
	private static Logger logger = LoggerFactory.getLogger(SMSSender.class);
	protected Properties properties;

	public SMSSender(Properties properties) {
		this.properties = properties;
	}

	public abstract void send(List<String> smsToList, String messageString);

}
