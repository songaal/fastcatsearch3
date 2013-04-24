package org.fastcatsearch.transport.common;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class MessageCounter extends SimpleChannelHandler {
	private static Logger logger = LoggerFactory.getLogger(MessageCounter.class);
	
    private final String id;
    private final AtomicLong writtenMessages;
    private final AtomicLong readMessages;

    public MessageCounter(String id) {
        this.id = id;
        this.writtenMessages = new AtomicLong();
        this.readMessages = new AtomicLong();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
//    	logger.debug("messageReceived >> {}, {}", ctx, e);
        this.readMessages.incrementAndGet();
        super.messageReceived(ctx, e);
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        this.writtenMessages.incrementAndGet();
        super.writeRequested(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        super.channelClosed(ctx, e);
        logger.debug("{} {} -> sent: {}b, recv: {}b", new Object[]{id, ctx.getChannel(), writtenMessages, readMessages});
    }

    public long getWrittenMessages() {
        return writtenMessages.get();
    }

    public long getReadMessages() {
        return readMessages.get();
    }
}
