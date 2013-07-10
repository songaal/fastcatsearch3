package org.fastcatsearch.ir.group;


public class GroupHit {
	protected GroupData groupData;
	protected int searchTotalCount;
	
	public GroupHit(GroupData groupData, int searchTotalCount){
		this.groupData = groupData;
		this.searchTotalCount = searchTotalCount;
	}
	
	public GroupData groupData(){
		return groupData;
	}
	
	public int totalCount(){
		return searchTotalCount;
	}
}
