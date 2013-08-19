package org.fastcatsearch.http.service.action;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import org.fastcatsearch.ir.io.ByteRefArrayOutputStream;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class ActionResponse {

	private Object contentType;
	// private byte[] content;
	// private int offset;
	// private int length;
	private HttpResponseStatus status;
	private PrintWriter writer;
	ByteRefArrayOutputStream baos;
	boolean isEmpty;

	public ActionResponse() {
		baos = new ByteRefArrayOutputStream();
		writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(baos)));
	}

	public ActionResponse(HttpResponseStatus status) {
		setStatus(status);
		contentType = "text/html";
		isEmpty = true;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setStatus(HttpResponseStatus status) {
		this.status = status;
	}

	public boolean contentThreadSafe() {
		return false;
	}

	public HttpResponseStatus status() {
		return status;
	}

	public PrintWriter getWriter() {
		return writer;

	}

	public void close() {
		if (!isEmpty) {
			writer.close();
		}
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
