package org.fastcatsearch.service.action;

import java.io.Writer;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class ActionResponse {
	private Object contentType;
	private byte[] content;
	private int offset;
	private int length;

	public void setContentType(String contentType){
		this.contentType = contentType;
	}
	
	public boolean contentThreadSafe(){
		return false;
	}
	
	public HttpResponseStatus status() {
		return null;
	}

	public Writer getWriter(){
		//TODO 
		//쉽게 쓸수있는 String Writer를 넘겨준다.
		//
		
		return null;
		
	}
	public void setContent(byte[] content, int offset, int length){
		this.content = content;
		this.offset = offset;
		this.length = length;
	}
	public byte[] content() {
		return content;
	}

	public int contentOffset() {
		return offset;
	}

	public int contentLength() {
		return length;
	}

	public Object contentType() {
		return contentType;
	}
}
