package org.fastcatsearch.job.state;

import java.util.Date;

import org.fastcatsearch.ir.util.Formatter;

public abstract class TaskState {
	public static final String STATE_STARTED = "STARTED";
	public static final String STATE_FINISHED = "FINISHED";
	
	protected TaskStateService taskStateService;
	protected String state;
//	private boolean isRunning;
	private long startTime;
	private String startTimeString;
	protected TaskKey taskKey;
	protected int progressRate; // 100이하.
	
	public TaskState(TaskKey taskKey, TaskStateService taskStateService){
		this.taskKey = taskKey;
		this.taskStateService = taskStateService;
	}
	
	public TaskKey taskKey(){
		return taskKey;
	}
	
//	public boolean isRunning() {
//		return isRunning;
//	}
	
	public String getElapsedTime() {
		return Formatter.getFormatTime((System.nanoTime() - startTime) / 1000000);
	}
	
	public String getStartTime(){
		return startTimeString;
	}
	
	public void start() {
		if (state == null) {
			state = STATE_STARTED;
			startTime = System.nanoTime();
			startTimeString = Formatter.formatDate(new Date(System.currentTimeMillis()));
		}
	}

	public void finish() {
		if (state != null) {
			state = STATE_FINISHED;
			taskStateService.remove(taskKey);
		}
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state){
		this.state = state;
	}

	public int getProgressRate(){
		return progressRate;
	}
	
	public void setProgressRate(int progressRate){
		this.progressRate = progressRate;
	}
	
	public abstract String getSummary();
	
}
