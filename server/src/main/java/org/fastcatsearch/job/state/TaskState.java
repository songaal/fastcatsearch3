package org.fastcatsearch.job.state;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.util.Formatter;

import java.io.IOException;
import java.util.Date;

public abstract class TaskState implements Streamable {
	public static final String STATE_NOT_STARTED = "NOT STARTED";
	public static final String STATE_RUNNING = "RUNNING";
	public static final String STATE_SUCCESS = "SUCCESS";
	public static final String STATE_FAIL = "FAIL";
	public static final String STATE_CANCEL = "CANCEL";
	public static final String STATE_STOP_REQUESTED = "STOP REQUESTED";
	
	protected boolean isScheduled;
	
	protected String state; //상태변수. 시작,종료,실패 여부.
	protected String step; //실제 진행 상황. 어떤 작업을 하고 있는지.
	
	private long startTime;
	private long endTime;
	protected int progressRate; // 100이하.

	public TaskState() {
		state = STATE_NOT_STARTED;
	}

	public TaskState(boolean isScheduled) {
		this();
		this.isScheduled = isScheduled;
	}

    public String toString() {
        return "[TaskState]" + state + "/" + step + "/" + isScheduled + "/" + getStartTime() + "~" + getEndTime();
    }

	public boolean isScheduled() {
		return isScheduled;
	}

	public String getElapsedTime() {
		if (isRunning()) {
			return Formatter.getFormatTime(System.currentTimeMillis() - startTime);
		} else if (isFinished()) {
			return Formatter.getFormatTime(endTime - startTime);
		} else {
			return "";
		}
	}
	
	public String getStartTime() {
		return Formatter.formatDate(new Date(startTime));
	}
	
	public String getEndTime() {
		if (endTime > 0) {
			return Formatter.formatDate(new Date(endTime));
		} else {
			return "";
		}
	}

	public boolean isRunning() {
		return STATE_RUNNING.equalsIgnoreCase(state);
	}
	
	public boolean isFinished() {
		return STATE_SUCCESS.equalsIgnoreCase(state) || STATE_FAIL.equalsIgnoreCase(state) || STATE_CANCEL.equalsIgnoreCase(state);
	}

	public boolean isOldTask() {
		return endTime > 0 && (System.currentTimeMillis() - endTime >= 3600000);
	}

	public void setStep(String step) {
		this.step = step;
	}
	
	public String getStep() {
		return step;
	}
	
	public void start() {
		if (!isRunning()) {
			state = STATE_RUNNING;
			startTime = System.currentTimeMillis();
		}else {
			throw new IllegalStateException("Task is already started state.");
		}
	}

	public void finishSuccess() {
		endTime = System.currentTimeMillis();
		this.state = STATE_SUCCESS;
	}
	
	public void finishFail() {
		endTime = System.currentTimeMillis();
		this.state = STATE_FAIL;
	}
	
	public void finishCancel() {
		endTime = System.currentTimeMillis();
		this.state = STATE_CANCEL;
	}
	
	public void requestStopState() {
		this.state = STATE_STOP_REQUESTED;
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
		step = input.readString();
		startTime = input.readLong();
		endTime = input.readLong();
		progressRate = input.readInt();
		
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeBoolean(isScheduled);
		output.writeString(state);
		output.writeString(step);
		output.writeLong(startTime);
		output.writeLong(endTime);
		output.writeInt(progressRate);
	}
}
