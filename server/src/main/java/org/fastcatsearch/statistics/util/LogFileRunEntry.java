package org.fastcatsearch.statistics.util;

public class LogFileRunEntry extends RunEntry {
	private String key;
	private int count;

	public LogFileRunEntry(String key, int count) {
		this.key = key;
		this.count = count;
	}

	public String getKey() {
		return key;
	}

	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "Entry " + key + " : " + count;
	}

}
