package org.fastcatsearch.util;

public interface ResultWriter {
	public ResultWriter object() throws ResultWriterException;
	public ResultWriter endObject() throws ResultWriterException;
	public ResultWriter array(String arrayName) throws ResultWriterException;
	public ResultWriter endArray() throws ResultWriterException;
	public ResultWriter key (String key) throws ResultWriterException;
	public ResultWriter value (Object obj) throws ResultWriterException;
	public void done();
}