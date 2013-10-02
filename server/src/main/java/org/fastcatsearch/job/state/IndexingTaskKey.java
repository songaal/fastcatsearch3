package org.fastcatsearch.job.state;

import org.fastcatsearch.ir.common.IndexingType;

public class IndexingTaskKey extends TaskKey {

	private String collectionId;
	private String indexingType;
		
	public IndexingTaskKey(String collectionId, IndexingType type) {
		this(collectionId, type, true);
	}
	
	public IndexingTaskKey(String collectionId, IndexingType type, boolean isScheduled) {
		super(isScheduled);
		this.collectionId = collectionId;
		this.indexingType = type.toString();
		key = collectionId + ">" + indexingType;
	}

	@Override
	public TaskState createState(TaskStateService taskStateService) {
		return new IndexingTaskState(this, taskStateService);
	}

	public String collectionId(){
		return collectionId;
	}
	
	public String indexingType(){
		return indexingType;
	}
}
