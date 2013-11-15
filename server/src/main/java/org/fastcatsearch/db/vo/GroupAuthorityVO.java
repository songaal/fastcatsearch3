package org.fastcatsearch.db.vo;

public class GroupAuthorityVO {
	public int groupId;
	public String authorityCode;
	public String authorityLevel;
	
	public GroupAuthorityVO(){ }
	
	public GroupAuthorityVO(int groupId, String authorityCode, String authorityLevel){
		this.groupId = groupId;
		this.authorityCode = authorityCode;
		this.authorityLevel = authorityLevel;
	}
	public int getGroupId() {
		return groupId;
	}
	
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
}
