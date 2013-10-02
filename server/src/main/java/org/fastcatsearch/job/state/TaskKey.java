package org.fastcatsearch.job.state;

public abstract class TaskKey {
	protected String key;
	protected boolean isScheduled;
	
	public TaskKey(boolean isScheduled){
		this.isScheduled = isScheduled;
	}
	
	public String getKey() {
		return key;
	}

	public boolean isScheduled(){
		return isScheduled;
	}
	
	public abstract TaskState createState(TaskStateService taskStateService);
	
	@Override
	public int hashCode(){
		return key.hashCode();
	}
	@Override
	public boolean equals(Object other){
		return key.equals(((TaskKey) other).key);
	}
}
