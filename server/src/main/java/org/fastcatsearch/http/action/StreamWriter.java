package org.fastcatsearch.http.action;

import java.io.IOException;

import org.fastcatsearch.http.HttpChannel;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamWriter {
	private static Logger logger = LoggerFactory.getLogger(StreamWriter.class);
	
	private HttpChannel httpChannel;

	public StreamWriter(HttpChannel httpChannel) {
		this.httpChannel = httpChannel;
	}

	public void write(byte[] data, int offest, int length) throws IOException {
		ChannelBuffer channelBuffer = ChannelBuffers.wrappedBuffer(data, offest, length);
		ChannelFuture channelFuture = httpChannel.channel().write(channelBuffer);
		try {
			channelFuture.await();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		if(!channelFuture.isSuccess()){
			logger.error("stream write fail. connected[{}]", channelFuture.getChannel().isConnected());
			throw new IOException();
		}
	}

	public void writeHeader(ActionResponse response) throws IOException {
		httpChannel.sendHeader(response);

	}

	public void close() throws IOException {
		httpChannel.channel().close();
	}

}
