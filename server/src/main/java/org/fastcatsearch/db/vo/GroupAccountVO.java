package org.fastcatsearch.db.vo;

public class GroupAccountVO {
	
	public static final String ADMIN_GROUP_NAME = "Administrators (Built-In)"; 
	
	public int id;
	public String groupName;
	
	public GroupAccountVO(){}
	public GroupAccountVO(String groupName){
		this.groupName = groupName;
	}
}