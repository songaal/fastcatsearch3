package org.fastcatsearch.util;

public interface ResultWriter {
	public ResultWriter object() throws StringifyException;
	public ResultWriter endObject() throws StringifyException;
	public ResultWriter array(String arrayName) throws StringifyException;
	public ResultWriter endArray() throws StringifyException;
	public ResultWriter key (String key) throws StringifyException;
	public ResultWriter value (Object obj) throws StringifyException;
	public void done();
}