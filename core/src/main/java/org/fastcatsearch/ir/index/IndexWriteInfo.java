package org.fastcatsearch.ir.index;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/*
 * 색인시 append되는 파일의 길이정보등을 저장한다.
 * 타 노드로 mirron sync시 색인파일의 어느부분을 보내야할지를 이 정보를 보고 판단하게 된다.
 * 그리고, 이전 revision복구시에도 사용된다.  
 * */
public class IndexWriteInfo {
	private String filename;
	private long offset;
	private long limit;
	private Map<String, Object> info;
	
	public IndexWriteInfo(File f){
		filename = f.getName();
		offset = f.length();
	}
	
	@Deprecated
	public IndexWriteInfo(long offset){
		this.offset = offset;
	}
	
	@Override
	public String toString(){
		return "["+getClass().getSimpleName()+"]filename["+filename+"] offset["+offset+"] length["+limit+"] info="+info;
	}
	
	public void close(long limit){
		this.limit = limit;
	}
	
	public String filename(){
		return filename;
	}
	
	public long offset(){
		return offset;
	}
	
	public long limit(){
		return limit;
	}
	
	public Object get(String key){
		if(info == null){
			return null;
		}
		
		return info.get(key);
	}
	
	public void put(String key, Object value){
		if(info == null){
			info = new HashMap<String, Object>(2);
		}
		info.put(key, value);
		
	}
	
	
}
