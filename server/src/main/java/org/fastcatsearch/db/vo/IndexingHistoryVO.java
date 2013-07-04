package org.fastcatsearch.db.vo;

import java.sql.Timestamp;

public class IndexingHistoryVO {
	public int id;
	public String collection;
	public String type;
	public boolean isSuccess;
	public int docSize;
	public int updateSize;
	public int deleteSize;
	public boolean isScheduled;
	public Timestamp startTime;
	public Timestamp endTime;
	public int duration;

}
