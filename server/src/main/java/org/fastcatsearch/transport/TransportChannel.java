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

package org.fastcatsearch.transport;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import org.fastcatsearch.common.io.BytesStreamOutput;
import org.fastcatsearch.common.io.CachedStreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.transport.vo.StreamableThrowable;

/**
 *
 */
public class TransportChannel {

    private final Channel channel;

    private final long requestId;

    public TransportChannel(Channel channel, long requestId) {
        this.channel = channel;
        this.requestId = requestId;
    }
    
    public void sendResponse(Object obj) throws IOException {
    	byte type = 0;
    	type = TransportOption.setTypeMessage(type);
    	byte status = 0;
    	status = TransportOption.setResponse(status);
    	status = TransportOption.setResponseObject(status);
    	byte resType = 0;
    	resType = TransportOption.setResponseObject(resType);
        CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
        BytesStreamOutput stream = cachedEntry.bytes();
        stream.skip(MessageProtocol.HEADER_SIZE);
        stream.writeGenericValue(obj);
        stream.close();
        
        ChannelBuffer buffer = stream.bytesReference().toChannelBuffer();
        MessageProtocol.writeHeader(buffer, type, requestId, status);
        ChannelFuture future = channel.write(buffer);
        future.addListener(new TransportModule.CacheFutureListener(cachedEntry));
    }
    public void sendResponse(Streamable response) throws IOException {
    	byte type = 0;
    	type = TransportOption.setTypeMessage(type);
    	byte status = 0;
    	status = TransportOption.setResponse(status);
        CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
        BytesStreamOutput stream = cachedEntry.bytes();
        stream.skip(MessageProtocol.HEADER_SIZE);
        stream.writeString(response.getClass().getName());
        response.writeTo(stream);
        stream.close();
        
        ChannelBuffer buffer = stream.bytesReference().toChannelBuffer();
        MessageProtocol.writeHeader(buffer, type, requestId, status);
        ChannelFuture future = channel.write(buffer);
        future.addListener(new TransportModule.CacheFutureListener(cachedEntry));
    }
    
    public void sendResponse(Throwable error) throws IOException {
    	byte type = 0;
    	type = TransportOption.setTypeMessage(type);
    	byte status = 0;
        status = TransportOption.setErrorResponse(status);
        
        CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
        BytesStreamOutput stream = cachedEntry.bytes();
        stream.skip(MessageProtocol.HEADER_SIZE);
        StreamableThrowable streamableThrowable = new StreamableThrowable(error);
        streamableThrowable.writeTo(stream);
        stream.close();
        
        ChannelBuffer buffer = stream.bytesReference().toChannelBuffer();
        MessageProtocol.writeHeader(buffer, type, requestId, status);
        ChannelFuture future = channel.write(buffer);
        future.addListener(new TransportModule.CacheFutureListener(cachedEntry));
    }
   
    	
}
