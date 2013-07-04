package org.fastcatsearch.transport.common;

import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class HandshakeHandler extends SimpleChannelHandler {
	protected static Logger logger = LoggerFactory.getLogger(HandshakeHandler.class);
	
	protected final long timeoutInMillis;
	protected final String localId;
    protected final AtomicBoolean handshakeComplete;
    protected final AtomicBoolean handshakeFailed;
    protected final CountDownLatch latch = new CountDownLatch(1);
    protected final Queue<MessageEvent> messages = new ArrayDeque<MessageEvent>();
    protected final Object handshakeMutex = new Object();
    protected String challenge;

    public HandshakeHandler(String localId, long timeoutInMillis) {
        this.localId = localId;
        this.timeoutInMillis = timeoutInMillis;
        this.handshakeComplete = new AtomicBoolean(false);
        this.handshakeFailed = new AtomicBoolean(false);
    }

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		logger.debug("Channel이 닫혔습니다.");
		if (!handshakeComplete.get()) {
			fireHandshakeFailed(ctx);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		logger.error("Channel에서 Exception 발생.", e.getCause());
		if (e.getChannel().isConnected()) {
			e.getChannel().close();
		} else {
			fireHandshakeFailed(ctx);
		}
	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		synchronized (handshakeMutex) {
			if (handshakeFailed.get()) {
				// handshake 가 실패였으면, 메시지를 보내지않고 무시한다.
				return;
			}

			if (handshakeComplete.get()) {
				super.writeRequested(ctx, e);
			} else {
				// handshake가 아직 완료되지 않았으면, 메시지를 큐에 쌓아둔다. 
				// 나중에 완료시 일괄 전송하게 된다.
				messages.offer(e);
			}
		}
	}

	protected void writeDownstream(ChannelHandlerContext ctx, Object data) {
        ChannelFuture f = Channels.succeededFuture(ctx.getChannel());
        SocketAddress address = ctx.getChannel().getRemoteAddress();
        Channel c = ctx.getChannel();
        ctx.sendDownstream(new DownstreamMessageEvent(c, f, data, address));
    }
	
	protected void fireHandshakeFailed(ChannelHandlerContext ctx) {
        handshakeComplete.set(true);
        handshakeFailed.set(true);
        latch.countDown();
        ctx.getChannel().close();
        ctx.sendUpstream(HandshakeEvent.handshakeFailed(ctx.getChannel()));
    }

	protected void fireHandshakeSucceeded(String server, ChannelHandlerContext ctx) {
        handshakeComplete.set(true);
        handshakeFailed.set(false);
        latch.countDown();
        ctx.sendUpstream(HandshakeEvent.handshakeSucceeded(server, ctx.getChannel()));
    }
}
