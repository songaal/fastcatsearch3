package org.fastcatsearch.http.action;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.fastcatsearch.http.HttpChannel;
import org.fastcatsearch.ir.io.ByteRefArrayOutputStream;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class ActionResponse {

	private final static Charset charset = Charset.forName("utf-8");
	private Object contentType;
	private HttpResponseStatus status;
	private ByteRefArrayOutputStream baos;
	private boolean isEmpty;

	private HttpChannel httpChannel;
	
	private String responseCookie;
	private String responseSetCookie;

	//스트림연결인지.
	private boolean isStreamResult;
	//객체유지용.
	private Writer writer;
	private StreamWriter streamWriter;
	
	public ActionResponse(HttpChannel httpChannel) {
		this.httpChannel = httpChannel;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setStatus(HttpResponseStatus status) {
		this.status = status;
	}

	public void setResponseCookie(String responseCookie) {
		this.responseCookie = responseCookie;
	}

	public void setResponseSetCookie(String responseSetCookie) {
		this.responseSetCookie = responseSetCookie;
	}

	public String responseCookie() {
		return responseCookie;
	}

	public String responseSetCookie() {
		return responseSetCookie;
	}

	public boolean contentThreadSafe() {
		return false;
	}

	public HttpResponseStatus status() {
		return status;
	}

	public OutputStream getOutputStream() {
		isEmpty = false;
		if(baos == null){
			baos = new ByteRefArrayOutputStream();
		}
		return baos;
	}

	public HttpChannel getChannel(){
		return httpChannel;
	}
	
	public Writer getWriter() {
		isEmpty = false;
		if(writer == null){
			if(baos == null){
				baos = new ByteRefArrayOutputStream();
			}
			writer = new BufferedWriter(new OutputStreamWriter(baos, charset));
		}
		
		return writer;
	}

	public Writer getExistWriter() {
		return writer;
	}
	
	public StreamWriter getStreamWriter() {
		if(streamWriter == null){
			streamWriter = new StreamWriter(httpChannel);
		}
		isStreamResult = true;
		return streamWriter;
	}

	public byte[] content() {
		if (!isEmpty) {
			return baos.array();
		} else {
			return null;
		}
	}

	public int contentOffset() {
		return 0;
	}

	public int contentLength() {
		if (!isEmpty) {
			return baos.length();
		} else {
			return 0;
		}
	}

	public Object contentType() {
		return contentType;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public void done() throws IOException{
		if(isStreamResult) {
			streamWriter.close();
		}else{
			httpChannel.sendResponse(this);
		}
	}
	
	public void error(Throwable e){
		if(isStreamResult) {
			httpChannel.channel().write(e.toString());
			httpChannel.channel().close();
		}else{
			httpChannel.sendError(HttpResponseStatus.INTERNAL_SERVER_ERROR, e);
		}
	}
}
