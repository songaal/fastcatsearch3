package org.fastcatsearch.job;


public abstract class ScheduledJob extends Job {

	private static final long serialVersionUID = -7623004697820458591L;
	
	public String key(){
		return (String) args;
	}
	
	public ScheduledJob(String key) {
		this.args = key;
	}
	
	public abstract void cancel();
	
	public abstract boolean isCanceled();
	
}
