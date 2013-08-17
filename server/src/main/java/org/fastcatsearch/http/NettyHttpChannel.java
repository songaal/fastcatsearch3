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

import org.fastcatsearch.service.action.ActionResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
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
	private final org.jboss.netty.handler.codec.http.HttpRequest request;

	public NettyHttpChannel(Channel channel, org.jboss.netty.handler.codec.http.HttpRequest request) {
		this.channel = channel;
		this.request = request;
	}

	public void sendResponse(ActionResponse response) {
		response.flush();

		// Decide whether to close the connection or not.
		boolean http10 = request.getProtocolVersion().equals(HttpVersion.HTTP_1_0);
		boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION))
				|| (http10 && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)));

		// Build the response object.
		HttpResponseStatus status = response.status();
		org.jboss.netty.handler.codec.http.HttpResponse resp;
		if (http10) {
			resp = new DefaultHttpResponse(HttpVersion.HTTP_1_0, status);
			if (!close) {
				resp.addHeader(HttpHeaders.Names.CONNECTION, "Keep-Alive");
			}
		} else {
			resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		}

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

		// Write the response.
		ChannelFuture future = channel.write(resp);

		// Close the connection after the write operation is done if necessary.
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

}
