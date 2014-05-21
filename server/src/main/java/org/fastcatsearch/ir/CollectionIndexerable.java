package org.fastcatsearch.ir;

import org.fastcatsearch.job.state.IndexingTaskState;


public interface CollectionIndexerable {
	public void requestStop();
	public boolean close() throws Exception;
	public void doIndexing() throws Exception;
	public void setTaskState(IndexingTaskState indexingTaskState);
	
}
