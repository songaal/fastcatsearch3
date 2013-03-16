package org.fastcatsearch.transport.common;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import org.fastcatsearch.transport.MessageProtocol;

public class ReadableFrameDecoder extends FrameDecoder {

	@Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		
        if (buffer.readableBytes() < MessageProtocol.HEADER_SIZE) {
            return null;
        }
        
        int readerIndex = buffer.readerIndex();
        
		if(MessageProtocol.isBufferReady(buffer, readerIndex)){
			buffer.skipBytes(2); //2byte 헤더 prefix만 스킵.
			return buffer;
		}else{
			return null;
		}
      
    }
}
