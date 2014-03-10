package org.fastcatsearch.util;

public interface ResponseWriter {
	public ResponseWriter object() throws ResultWriterException;
	public ResponseWriter endObject() throws ResultWriterException;
	public ResponseWriter array() throws ResultWriterException;
	/**
	 * xml에서는 array 의 item에 대해서 tag를 감싸주어야 하므로 해당 tag명을 셋팅한다. json에서는 필요없다.
	 * */
	public ResponseWriter array(String arrayName) throws ResultWriterException;
	public ResponseWriter endArray() throws ResultWriterException;
	public ResponseWriter key (String key) throws ResultWriterException;
	public ResponseWriter value (Object obj) throws ResultWriterException;
	
	//마무리 조치만 취하면 backed writer를 close하지는 않는다.
	public void done();
}