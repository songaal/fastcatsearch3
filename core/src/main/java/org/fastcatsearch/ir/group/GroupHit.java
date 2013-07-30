package org.fastcatsearch.ir.group;


public class GroupHit {
	protected GroupsData groupData;
	protected int searchTotalCount;
	
	public GroupHit(GroupsData groupData, int searchTotalCount){
		this.groupData = groupData;
		this.searchTotalCount = searchTotalCount;
	}
	
	public GroupsData groupData(){
		return groupData;
	}
	
	public int totalCount(){
		return searchTotalCount;
	}
}
