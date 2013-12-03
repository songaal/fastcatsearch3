package org.fastcatsearch.transport.common;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class ByteCounter extends SimpleChannelUpstreamHandler {
	private static Logger logger = LoggerFactory.getLogger(ByteCounter.class);
	
    private final String id;
    private final AtomicLong writtenBytes;
    private final AtomicLong readBytes;

    public ByteCounter(String id) {
        this.id = id;
        this.writtenBytes = new AtomicLong();
        this.readBytes = new AtomicLong();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
//    	logger.debug("messageReceived >> {}, {}", ctx, e);
        if (e.getMessage() instanceof ChannelBuffer) {
            this.readBytes.addAndGet(((ChannelBuffer) e.getMessage()).readableBytes());
//            logger.debug("ByteCounter {} -> sent: {}b, recv: {}b", new Object[]{id, writtenBytes, readBytes});
        }

        super.messageReceived(ctx, e);
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e)
            throws Exception {
        super.writeComplete(ctx, e);
        this.writtenBytes.addAndGet(e.getWrittenAmount());
    }
    @Override
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
        logger.debug("Connected! {} {}", id, ctx.getChannel());
    }
    public void channelOpen(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
//        logger.debug("Opened! {} {}", id, ctx.getChannel());
    }
    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        super.channelClosed(ctx, e);
        logger.debug("CLOSED! {} {} -> sent: {}b, recv: {}b", new Object[]{id, ctx.getChannel(), writtenBytes, readBytes});
    }
    public void channelDisconnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
//        logger.debug("Disconnected! {} {}", id, ctx.getChannel());
    }

    public long getWrittenBytes() {
        return writtenBytes.get();
    }

    public long getReadBytes() {
        return readBytes.get();
    }
}
