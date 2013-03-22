package org.fastcatsearch.cli;

public interface ConsoleSessionContext {
	
	public void setAttribute(String key, Object value);
	
	public Object getAttribute(String key);
}
