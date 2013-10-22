package org.fastcatsearch.db.vo;

import java.sql.Timestamp;

import org.fastcatsearch.db.mapper.IndexingResultMapper.ResultStatus;

public class TaskHistoryVO {
	public int id;
	public long taskId;
	public String executable;
	public String args;
	public ResultStatus status;
	public String resultStr;
	public boolean isScheduled;
	public Timestamp startTime;
	public Timestamp endTime;
	public int duration;
}
