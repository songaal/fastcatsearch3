package org.fastcatsearch.db.vo;

import java.sql.Timestamp;

public class SearchEventVO {
	public int id;
	public Timestamp when;
	public String type;
	public int category;
	public String summary;
	public String stacktrace;
	public String status;
}
