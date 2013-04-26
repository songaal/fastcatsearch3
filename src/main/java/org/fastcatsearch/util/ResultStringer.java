package org.fastcatsearch.util;

public interface ResultStringer {
	public ResultStringer object() throws StringifyException;
	public ResultStringer endObject() throws StringifyException;
	public ResultStringer array(String arrayName) throws StringifyException;
	public ResultStringer endArray() throws StringifyException;
	public ResultStringer key (String key) throws StringifyException;
	public ResultStringer value (Object obj) throws StringifyException;
}