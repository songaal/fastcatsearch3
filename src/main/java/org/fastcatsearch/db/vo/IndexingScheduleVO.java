package org.fastcatsearch.db.vo;

import java.sql.Timestamp;

public class IndexingScheduleVO {
	public String collection;
	public String type;
	public int period;
	public Timestamp startTime;
	public boolean isActive;

}
