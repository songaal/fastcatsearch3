package org.fastcatsearch.ir;


public interface CollectionIndexerable {
	public void requestStop();
	public boolean close() throws Exception;
	public void doIndexing() throws Exception;
	
}
