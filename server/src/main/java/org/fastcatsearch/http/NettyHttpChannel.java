/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.fastcatsearch.http;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.fastcatsearch.http.action.ActionResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class NettyHttpChannel implements HttpChannel {
	private static final Logger logger = LoggerFactory.getLogger(NettyHttpChannel.class);

	private final Channel channel;
	private final HttpRequest request;

	public NettyHttpChannel(Channel channel, HttpRequest request) {
		this.channel = channel;
		this.request = request;
	}

	@Override
	public Channel channel() {
		return channel;
	}

	@Override
	public void sendHeader(ActionResponse response) {
		// Decide whether to close the connection or not.
		boolean http10 = request.getProtocolVersion().equals(HttpVersion.HTTP_1_0);
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION))
				|| (http10 && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)));

		// Build the response object.
		HttpResponseStatus status = HttpResponseStatus.OK;
		HttpResponse resp = null;
		if (http10) {
			resp = new DefaultHttpResponse(HttpVersion.HTTP_1_0, status);
			if (!close) {
				resp.addHeader(HttpHeaders.Names.CONNECTION, "Keep-Alive");
			}
		} else {
			resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		}

		if (response.responseCookie() != null) {
			resp.addHeader(HttpHeaders.Names.COOKIE, response.responseCookie());
		}
		if (response.responseSetCookie() != null) {
			resp.addHeader(HttpHeaders.Names.SET_COOKIE, response.responseSetCookie());
		}
		resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, response.contentType());
	}

	@Override
	public void close() {
		boolean http10 = request.getProtocolVersion().equals(HttpVersion.HTTP_1_0);
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION))
				|| (http10 && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)));
		logger.debug("netty http close = {}", close);
		
		if (close) {
			channel.close();
		}
	}

	@Override
	public void sendResponse(ActionResponse response) {

		// Decide whether to close the connection or not.
		boolean http10 = request.getProtocolVersion().equals(HttpVersion.HTTP_1_0);
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION))
				|| (http10 && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)));

		// Build the response object.
		HttpResponseStatus status = response.status();
		HttpResponse resp = null;
		if (http10) {
			resp = new DefaultHttpResponse(HttpVersion.HTTP_1_0, status);
			if (!close) {
				resp.addHeader(HttpHeaders.Names.CONNECTION, "Keep-Alive");
			}
		} else {
			resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		}

		if (response.responseCookie() != null) {
			resp.addHeader(HttpHeaders.Names.COOKIE, response.responseCookie());
		}
		if (response.responseSetCookie() != null) {
			resp.addHeader(HttpHeaders.Names.SET_COOKIE, response.responseSetCookie());
		}

		if (!response.isEmpty()) {
			ChannelBuffer buf = null;
			if (response.contentThreadSafe()) {
				buf = ChannelBuffers.wrappedBuffer(response.content(), response.contentOffset(), response.contentLength());
			} else {
				buf = ChannelBuffers.copiedBuffer(response.content(), response.contentOffset(), response.contentLength());
			}
			//
			resp.setContent(buf);
			resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, response.contentType());

			resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
		}
		// Write the response.
		ChannelFuture future = channel.write(resp);

		// Close the connection after the write operation is done if necessary.
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void sendError(HttpResponseStatus status, Throwable throwable) {
		// Decide whether to close the connection or not.
		boolean http10 = request.getProtocolVersion().equals(HttpVersion.HTTP_1_0);
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION))
				|| (http10 && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)));

		// Build the response object.
		HttpResponse resp = null;
		if (http10) {
			resp = new DefaultHttpResponse(HttpVersion.HTTP_1_0, status);
			if (!close) {
				resp.addHeader(HttpHeaders.Names.CONNECTION, "Keep-Alive");
			}
		} else {
			resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		}

		String cause = getErrorHtml(status, throwable);
		byte[] errorMessage = cause.getBytes(Charset.forName("UTF-8"));
		ChannelBuffer buf = ChannelBuffers.wrappedBuffer(errorMessage);
		resp.setContent(buf);
		resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
		resp.setHeader(HttpHeaders.Names.CONTENT_ENCODING, "UTF-8");
		resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes());
		// Write the response.
		ChannelFuture future = channel.write(resp);

		// Close the connection after the write operation is done if necessary.
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private String getErrorHtml(HttpResponseStatus status, Throwable throwable) {
		String stackTrace = null;
		if (throwable != null) {
			StringWriter sw = new StringWriter();
			PrintWriter s = new PrintWriter(sw);
			throwable.printStackTrace(s);
			stackTrace = sw.toString();
		}
		return "<html>\n" + "<head>\n" + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n" + "<title>Error " + status.toString() + "</title>\n"
				+ "</head>\n" + "<body>\n" + "<h2>HTTP ERROR: " + status.getCode() + "</h2>\n" + "<p>Problem accessing [" + request.getMethod() + "]" + request.getUri()
				+ ". Reason:\n" + "<pre>    " + status.getReasonPhrase() + (stackTrace != null ? "\n\n" + stackTrace : "") + "</pre></p>\n"
				+ "<hr /><i><small>Powered by FastcatSearch</small></i>\n" + "</body>\n" + "</html>";
	}

}
