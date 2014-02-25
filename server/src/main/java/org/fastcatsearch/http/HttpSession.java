package org.fastcatsearch.http;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSession {
	private Map<String, Object> map;
	private String id;
	private long lastTime;
	
	public HttpSession(String id){
		this.id = id;
		map = new ConcurrentHashMap<String, Object>(1, 1.0f);
		lastTime = System.currentTimeMillis();
	}
	
	public String getId(){
		return id;
	}
	
	public long getLastTime(){
		return lastTime;
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
	
	public Map<String, Object> map(){
		return map;
	}

	public void update() {
		lastTime = System.currentTimeMillis();
	}
	
	public String toString() {
		return "[HttpSession] " + id + " : " + new Date(lastTime);
	}
}
