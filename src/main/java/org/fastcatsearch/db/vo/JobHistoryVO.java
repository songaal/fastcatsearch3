package org.fastcatsearch.db.vo;

import java.sql.Timestamp;

public class JobHistoryVO {
	public long id;
	public long jobId;
	public String jobClassName;
	public String args;
	public boolean isSuccess;
	public String resultStr;
	public boolean isScheduled;
	public Timestamp startTime;
	public Timestamp endTime;
	public int duration;

}
