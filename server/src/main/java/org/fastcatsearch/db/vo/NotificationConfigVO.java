package org.fastcatsearch.db.vo;

public class NotificationConfigVO {

	private int id;
	private String code;
	private String alertTo;
	
	
	public NotificationConfigVO(){
	}
	
	public NotificationConfigVO (String code, String alertTo){
		this.code = code;
		this.alertTo = alertTo;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAlertTo() {
		return alertTo;
	}

	public void setAlertTo(String alertTo) {
		this.alertTo = alertTo;
	}
	
	@Override
	public String toString(){
		return getClass().getSimpleName()+"] code["+code+"] alertTo[" + alertTo + "]";
	}
	
}
