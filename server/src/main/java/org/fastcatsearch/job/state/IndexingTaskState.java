package org.fastcatsearch.job.state;

public class IndexingTaskState extends TaskState {

	public static final String STATE_INITIALIZE = "INITIALIZE";
	public static final String STATE_INDEXING = "INDEXING";
	public static final String STATE_FILECOPY = "COPY FILE TO REMOTE NODE";
	public static final String STATE_FINALIZE = "FINALIZE";
	public static final String STATE_STOP_REQUESTED = "STOP REQUESTED";
	
	public IndexingTaskState(TaskKey taskKey, TaskStateService taskStateService) {
		super(taskKey, taskStateService);
	}
	
	private int documentCount;
	
	public int getDocumentCount() {
		return documentCount;
	}

	public void incrementDocumentCount() {
		documentCount++;
	}

	@Override
	public String getSummary() {
		IndexingTaskKey indexingTaskKey= (IndexingTaskKey) taskKey;
		return indexingTaskKey.isScheduled() ? "Scheduled " : "Manual " + indexingTaskKey.collectionId() + " Indexing " + documentCount;
	}
	
	@Override
	public int getProgressRate(){
		return -1; //-1은 진행율 표시하지 않음.
	}
}
