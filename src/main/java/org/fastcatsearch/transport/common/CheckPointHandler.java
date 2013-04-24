package org.fastcatsearch.transport.common;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckPointHandler extends SimpleChannelHandler {

	private Logger logger;
	
	public CheckPointHandler(String name){
		logger = LoggerFactory.getLogger(name);
	}
	
	
	@Override
	public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {

		logger.debug("{} handleUpstream e={}", this, e);
		
        if (e instanceof MessageEvent) {
        	logger.debug("MessageEvent >> messageReceived");
            messageReceived(ctx, (MessageEvent) e);
        } else if (e instanceof WriteCompletionEvent) {
        	logger.debug("WriteCompletionEvent >> writeComplete");
            WriteCompletionEvent evt = (WriteCompletionEvent) e;
            writeComplete(ctx, evt);
        } else if (e instanceof ChildChannelStateEvent) {
            ChildChannelStateEvent evt = (ChildChannelStateEvent) e;
            if (evt.getChildChannel().isOpen()) {
            	logger.debug("ChildChannelStateEvent >> childChannelOpen");
                childChannelOpen(ctx, evt);
            } else {
            	logger.debug("ChildChannelStateEvent >> childChannelClosed");
                childChannelClosed(ctx, evt);
            }
        } else if (e instanceof ChannelStateEvent) {
            ChannelStateEvent evt = (ChannelStateEvent) e;
            switch (evt.getState()) {
            case OPEN:
                if (Boolean.TRUE.equals(evt.getValue())) {
                	logger.debug("ChannelStateEvent >> channelOpen");
                    channelOpen(ctx, evt);
                } else {
                	logger.debug("ChannelStateEvent >> channelClosed");
                    channelClosed(ctx, evt);
                }
                break;
            case BOUND:
                if (evt.getValue() != null) {
                	logger.debug("ChannelStateEvent >> channelBound");
                    channelBound(ctx, evt);
                } else {
                	logger.debug("ChannelStateEvent >> channelUnbound");
                    channelUnbound(ctx, evt);
                }
                break;
            case CONNECTED:
                if (evt.getValue() != null) {
                	logger.debug("ChannelStateEvent >> channelConnected");
                    channelConnected(ctx, evt);
                } else {
                	logger.debug("ChannelStateEvent >> channelDisconnected");
                    channelDisconnected(ctx, evt);
                }
                break;
            case INTEREST_OPS:
            	logger.debug("ChannelStateEvent >> channelInterestChanged");
                channelInterestChanged(ctx, evt);
                break;
            default:
            	logger.debug("ChannelStateEvent >> sendUpstream");
                ctx.sendUpstream(e);
            }
        } else if (e instanceof ExceptionEvent) {
        	logger.debug("ExceptionEvent >> exceptionCaught");
            exceptionCaught(ctx, (ExceptionEvent) e);
        } else {
        	logger.debug("------ >> sendUpstream");
            ctx.sendUpstream(e);
        }
    }

	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {

		
		logger.debug("handleDownstream e={}", e);
		
		
        if (e instanceof MessageEvent) {
        	logger.debug("MessageEvent >> writeRequested");
            writeRequested(ctx, (MessageEvent) e);
        } else if (e instanceof ChannelStateEvent) {
            ChannelStateEvent evt = (ChannelStateEvent) e;
            switch (evt.getState()) {
            case OPEN:
                if (!Boolean.TRUE.equals(evt.getValue())) {
                	logger.debug("ChannelStateEvent >> closeRequested");
                    closeRequested(ctx, evt);
                }
                break;
            case BOUND:
                if (evt.getValue() != null) {
                	logger.debug("ChannelStateEvent >> bindRequested");
                    bindRequested(ctx, evt);
                } else {
                	logger.debug("ChannelStateEvent >> unbindRequested");
                    unbindRequested(ctx, evt);
                }
                break;
            case CONNECTED:
                if (evt.getValue() != null) {
                	logger.debug("ChannelStateEvent >> connectRequested");
                    connectRequested(ctx, evt);
                } else {
                	logger.debug("ChannelStateEvent >> disconnectRequested");
                    disconnectRequested(ctx, evt);
                }
                break;
            case INTEREST_OPS:
            	logger.debug("ChannelStateEvent >> setInterestOpsRequested");
                setInterestOpsRequested(ctx, evt);
                break;
            default:
            	logger.debug("ChannelStateEvent >> sendDownstream");
                ctx.sendDownstream(e);
            }
        } else {
        	logger.debug("------ >> sendDownstream");
            ctx.sendDownstream(e);
        }
    }
}
