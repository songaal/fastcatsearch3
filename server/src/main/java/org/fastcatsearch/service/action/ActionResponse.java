package org.fastcatsearch.service.action;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import org.fastcatsearch.ir.io.ByteRefArrayOutputStream;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class ActionResponse {
	private Object contentType;
//	private byte[] content;
//	private int offset;
//	private int length;
	private HttpResponseStatus status;
	private PrintWriter writer;
	ByteRefArrayOutputStream baos;
	
	public ActionResponse(){
		baos = new ByteRefArrayOutputStream();
		writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(baos)));
	}
	
	public void setContentType(String contentType){
		this.contentType = contentType;
	}
	
	public void setStatus(HttpResponseStatus status){
		this.status = status;
	}
	
	public boolean contentThreadSafe(){
		return false;
	}
	
	public HttpResponseStatus status() {
		return status;
	}

	public Writer getWriter(){
		return writer;
		
	}
//	public void setContent(byte[] content, int offset, int length){
//		this.content = content;
//		this.offset = offset;
//		this.length = length;
//	}
	
	public void flush(){
		writer.flush();
	}
	
	public byte[] content() {
		return baos.array();
	}

	public int contentOffset() {
		return 0;
	}

	public int contentLength() {
		return baos.length();
	}

	public Object contentType() {
		return contentType;
	}
}
