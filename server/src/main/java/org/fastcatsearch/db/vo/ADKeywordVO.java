package org.fastcatsearch.db.vo;

import java.sql.Timestamp;


public class ADKeywordVO {
	
	private int id;
	private String keyword;
	private String value;
	private Timestamp updateTime;
	
	public ADKeywordVO(){
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	
	
}
