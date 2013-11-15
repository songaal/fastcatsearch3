package org.fastcatsearch.db.vo;

public class UserAccountVO {
	
	public int id;
	public String name;
	public String userId;
	public String password;
	public String email;
	public String sms;
	public int groupId;
	
	public UserAccountVO(){ }
	
	public UserAccountVO(String name, String userId, String password, String email, String sms, int groupId) {
		this.name = name;
		this.userId = userId;
		this.password = password;
		this.email = email;
		this.sms = sms;
		this.groupId = groupId;
	}
}
