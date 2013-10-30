package org.fastcatsearch.db.vo;

import java.sql.Timestamp;

public class DictionaryStatusVO {
	public String dictionaryId;
	public Timestamp updateTime;
	public Timestamp applyTime;
	public int applyEntrySize;
	
	public DictionaryStatusVO(){
	}
	
	public DictionaryStatusVO(String dictionaryId){
		this.dictionaryId = dictionaryId;
		updateTime = new Timestamp(0);
		applyTime = new Timestamp(0);
	}
	
	public String toString(){
		return getClass().getSimpleName()+" : " + dictionaryId +" : " + updateTime + " : " + applyTime + " : " + applyEntrySize;
	}
}
