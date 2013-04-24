package org.fastcatsearch.transport.common;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fastcatsearch.transport.MessageProtocol;

public class ReadableFrameDecoder extends FrameDecoder {

	private static Logger logger = LoggerFactory.getLogger(ReadableFrameDecoder.class);

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

		if (buffer.readableBytes() < MessageProtocol.HEADER_SIZE) {
			return null;
		}

		int readerIndex = buffer.readerIndex();
//		logger.debug("ctx = {}, channel={}", ctx, channel);
		if (MessageProtocol.isBufferReady(buffer, readerIndex)) {
			buffer.skipBytes(2); // 2byte 헤더 prefix만 스킵.
			return buffer;
		} else {
			return null;
		}

	}
}
