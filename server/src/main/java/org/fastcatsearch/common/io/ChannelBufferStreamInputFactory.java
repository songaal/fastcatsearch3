package org.fastcatsearch.common.io;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.transport.ChannelBufferStreamInput;
import org.jboss.netty.buffer.ChannelBuffer;

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
