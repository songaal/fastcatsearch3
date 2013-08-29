package org.fastcatsearch.http;

import java.util.concurrent.ConcurrentHashMap;

public class HttpSession {
	private ConcurrentHashMap<String, Object> map;
	private String id;
	private long createTime;
	
	public HttpSession(String id){
		this.id = id;
		map = new ConcurrentHashMap<String, Object>();
		createTime = System.currentTimeMillis();
	}
	
	public String getId(){
		return id;
	}
	
	public long getCreationTime(){
		return createTime;
	}
	
	public Object getAttribute(String name){
		return map.get(name);
	}
	
	public void setAttribute(String name, Object value){
		map.put(name, value);
	}
	
	public void removeAttribute(String name){
		map.remove(name);
	}
	
	public void invalidate(){
		map.clear();
	}
	
	public ConcurrentHashMap<String, Object> map(){
		return map;
	}
}
