package org.fastcatsearch.job.state;

import java.io.IOException;
import java.util.Date;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.util.Formatter;

public abstract class TaskState implements Streamable {
	public static final String STATE_STARTED = "STARTED";
	public static final String STATE_FINISHED = "FINISHED";
	
	protected boolean isScheduled;
	protected String state;
	private long startTime;
	private String startTimeString;
	protected int progressRate; // 100이하.

	public TaskState(){
	}
	
	public TaskState(boolean isScheduled){
		this.isScheduled = isScheduled;
	}
	
	public boolean isScheduled(){
		return isScheduled;
	}

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
		}
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state){
		this.state = state;
	}
	
	public void addState(String state){
		if(this.state != null){
			this.state += (" > " +state); 
		}else{
			this.state = state;
		}
	}

	public int getProgressRate(){
		return progressRate;
	}
	
	public void setProgressRate(int progressRate){
		this.progressRate = progressRate;
	}
	
	public abstract String getSummary();
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		isScheduled = input.readBoolean();
		state = input.readString();
		startTime = input.readLong();
		startTimeString = input.readString();
		progressRate = input.readInt();
		
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeBoolean(isScheduled);
		output.writeString(state);
		output.writeLong(startTime);
		output.writeString(startTimeString);
		output.writeInt(progressRate);
	}
}
