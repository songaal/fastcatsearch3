package org.fastcatsearch.common.io;

import org.jboss.netty.buffer.ChannelBuffer;

import org.fastcatsearch.transport.ChannelBufferStreamInput;

/**
 */
public class ChannelBufferStreamInputFactory {

    public static StreamInput create(ChannelBuffer buffer) {
        return new ChannelBufferStreamInput(buffer, buffer.readableBytes());
    }

    public static StreamInput create(ChannelBuffer buffer, int size) {
        return new ChannelBufferStreamInput(buffer, size);
    }
}
