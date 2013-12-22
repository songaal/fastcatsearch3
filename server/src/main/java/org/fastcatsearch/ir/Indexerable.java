package org.fastcatsearch.ir;


public interface Indexerable {
	public void requestStop();
	public boolean close() throws Exception;
	public void doIndexing() throws Exception;
	
}
