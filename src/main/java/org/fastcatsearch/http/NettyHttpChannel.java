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

import java.io.IOException;
import java.util.Set;

import org.fastcatsearch.service.action.ActionResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 *
 */
public class NettyHttpChannel implements HttpChannel {
//    private final HttpTransportModule transport;
    private final Channel channel;
    private final org.jboss.netty.handler.codec.http.HttpRequest request;

    public NettyHttpChannel(Channel channel, org.jboss.netty.handler.codec.http.HttpRequest request) {
//        this.transport = transport;
        this.channel = channel;
        this.request = request;
    }

    public void sendResponse(ActionResponse response) {

        // Decide whether to close the connection or not.
        boolean http10 = request.getProtocolVersion().equals(HttpVersion.HTTP_1_0);
        boolean close =
                HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) ||
                        (http10 && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)));

        // Build the response object.
//        HttpResponseStatus status = getStatus(response.status());
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
//        if (RestUtils.isBrowser(request.getHeader(HttpHeaders.Names.USER_AGENT))) {
//            if (transport.settings().getAsBoolean("http.cors.enabled", true)) {
//                // Add support for cross-origin Ajax requests (CORS)
//                resp.addHeader("Access-Control-Allow-Origin", transport.settings().get("http.cors.allow-origin", "*"));
//                if (request.getMethod() == HttpMethod.OPTIONS) {
//                    // Allow Ajax requests based on the CORS "preflight" request
//                    resp.addHeader("Access-Control-Max-Age", transport.settings().getAsInt("http.cors.max-age", 1728000));
//                    resp.addHeader("Access-Control-Allow-Methods", transport.settings().get("http.cors.allow-methods", "OPTIONS, HEAD, GET, POST, PUT, DELETE"));
//                    resp.addHeader("Access-Control-Allow-Headers", transport.settings().get("http.cors.allow-headers", "X-Requested-With, Content-Type, Content-Length"));
//                }
//            }
//        }

        String opaque = request.getHeader("X-Opaque-Id");
        if (opaque != null) {
            resp.addHeader("X-Opaque-Id", opaque);
        }

        // Convert the response content to a ChannelBuffer.
        ChannelFutureListener releaseContentListener = null;
        ChannelBuffer buf;
//        try {
//            if (response instanceof XContentRestResponse) {
//                // if its a builder based response, and it was created with a CachedStreamOutput, we can release it
//                // after we write the response, and no need to do an extra copy because its not thread safe
//                XContentBuilder builder = ((XContentRestResponse) response).builder();
//                if (builder.payload() instanceof CachedStreamOutput.Entry) {
//                    releaseContentListener = new NettyTransport.CacheFutureListener((CachedStreamOutput.Entry) builder.payload());
//                    buf = builder.bytes().toChannelBuffer();
//                } else if (response.contentThreadSafe()) {
//                    buf = ChannelBuffers.wrappedBuffer(response.content(), response.contentOffset(), response.contentLength());
//                } else {
//                    buf = ChannelBuffers.copiedBuffer(response.content(), response.contentOffset(), response.contentLength());
//                }
//            } else {
                if (response.contentThreadSafe()) {
                    buf = ChannelBuffers.wrappedBuffer(response.content(), response.contentOffset(), response.contentLength());
                } else {
                    buf = ChannelBuffers.copiedBuffer(response.content(), response.contentOffset(), response.contentLength());
                }
//            }
//        } catch (IOException e) {
//            throw new HttpException("Failed to convert response to bytes", e);
//        }
//        if (response.prefixContent() != null || response.suffixContent() != null) {
//            ChannelBuffer prefixBuf = ChannelBuffers.EMPTY_BUFFER;
//            if (response.prefixContent() != null) {
//                prefixBuf = ChannelBuffers.copiedBuffer(response.prefixContent(), response.prefixContentOffset(), response.prefixContentLength());
//            }
//            ChannelBuffer suffixBuf = ChannelBuffers.EMPTY_BUFFER;
//            if (response.suffixContent() != null) {
//                suffixBuf = ChannelBuffers.copiedBuffer(response.suffixContent(), response.suffixContentOffset(), response.suffixContentLength());
//            }
//            buf = ChannelBuffers.wrappedBuffer(prefixBuf, buf, suffixBuf);
//        }
        resp.setContent(buf);
        resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, response.contentType());

        resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));

//        if (transport.resetCookies) {
            String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
            if (cookieString != null) {
                CookieDecoder cookieDecoder = new CookieDecoder();
                Set<Cookie> cookies = cookieDecoder.decode(cookieString);
                if (!cookies.isEmpty()) {
                    // Reset the cookies if necessary.
                    CookieEncoder cookieEncoder = new CookieEncoder(true);
                    for (Cookie cookie : cookies) {
                        cookieEncoder.addCookie(cookie);
                    }
                    resp.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
                }
            }
//        }

        // Write the response.
        ChannelFuture future = channel.write(resp);
        if (releaseContentListener != null) {
            future.addListener(releaseContentListener);
        }

        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

 
}
