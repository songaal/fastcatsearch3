package org.fastcatsearch.ir.group;

import java.util.List;

/**
 * 여러그룹의 그룹핑 결과를 담는다.
 * */
public class GroupsData {
	private int totalSearchCount;
	private List<GroupEntryList> list;
	
	public GroupsData(List<GroupEntryList> list, int totalSearchCount){
		this.list = list;
		this.totalSearchCount = totalSearchCount;
	}
	
	public List<GroupEntryList> list(){
		return list;
	}
	
	public int groupSize(){
		return list.size();
	}
	
	public int totalSearchCount(){
		return totalSearchCount;
	}
	
	public GroupEntryList getGroupEntryList(int groupNo){
		return list.get(groupNo);
	}
	
	public GroupDataReader getGroupDataReader(int groupNo){
		return new GroupDataReader(list.get(groupNo), totalSearchCount);
	}
}
