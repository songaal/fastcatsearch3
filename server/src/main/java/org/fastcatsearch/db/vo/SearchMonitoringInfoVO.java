package org.fastcatsearch.db.vo;

import java.sql.Timestamp;

public class SearchMonitoringInfoVO {
	public int id;
	public String collection;
	public int hit;
	public int fail;
	public int achit;
	public int acfail;
	public int ave_time;
	public int max_time;
	public Timestamp when;
	public String type;
}
