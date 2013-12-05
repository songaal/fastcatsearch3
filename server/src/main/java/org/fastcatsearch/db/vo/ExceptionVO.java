package org.fastcatsearch.db.vo;

import java.sql.Timestamp;

public class ExceptionVO {
	
	private int id;
	private String node;
	private String message;
	private String trace;
	private Timestamp regtime;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		if(message == null){
			message = "";
		}
		this.message = message;
	}
	public String getTrace() {
		return trace;
	}
	public void setTrace(String trace) {
		if(trace == null){
			trace = "";
		}
		this.trace = trace;
	}
	public Timestamp getRegtime() {
		return regtime;
	}
	public void setRegtime(Timestamp regtime) {
		this.regtime = regtime;
	}
	
}
