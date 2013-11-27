package org.fastcatsearch.ir.index;

import java.io.File;

/*
 * 색인시 append되는 파일의 길이정보등을 저장한다.
 * 타 노드로 mirron sync시 색인파일의 어느부분을 보내야할지를 이 정보를 보고 판단하게 된다.
 * 그리고, 이전 revision복구시에도 사용된다.  
 * */
public class IndexWriteInfo {
	private String filename;
	private long offset;
	private long limit;
	
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
		return "["+getClass().getSimpleName()+"] filename["+filename+"] offset["+offset+"] length["+limit+"]";
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
	
}
