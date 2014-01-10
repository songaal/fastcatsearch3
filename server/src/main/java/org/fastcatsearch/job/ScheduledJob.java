package org.fastcatsearch.job;


public abstract class ScheduledJob extends Job {

	private static final long serialVersionUID = -7623004697820458591L;
	private String key;
	
	public String key(){
		return key;
	}
	
	public ScheduledJob(String key) {
		this.key = key;
	}
	
	public abstract void cancel();
	
	public abstract boolean isCanceled();
	
}
