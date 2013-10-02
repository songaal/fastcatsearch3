package org.fastcatsearch.http.action;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.fastcatsearch.ir.io.ByteRefArrayOutputStream;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class ActionResponse {

	private final static Charset charset = Charset.forName("UTF-8");
	private Object contentType;
	private HttpResponseStatus status;
	private ByteRefArrayOutputStream baos;
	private boolean isEmpty;

	private String responseCookie;
	private String responseSetCookie;

	public ActionResponse() {
	}

	public void init() {
		baos = new ByteRefArrayOutputStream();
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
		return baos;
	}

	public Writer getWriter() {
		return new BufferedWriter(new OutputStreamWriter(baos, charset));

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
		return false;
	}

}
