package org.fastcatsearch.transport.common;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.transport.ChannelBufferStreamInput;
import org.fastcatsearch.transport.TransportChannel;
import org.fastcatsearch.transport.TransportModule;
import org.fastcatsearch.transport.TransportOption;
import org.fastcatsearch.transport.vo.StreamableBoolean;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileChannelHandler extends SimpleChannelUpstreamHandler {

	private static Logger logger = LoggerFactory.getLogger(FileChannelHandler.class);
	private TransportModule transport;
	private FileTransportHandler fileHandler;

	private final Streamable FILE_RECEIVE_DONE = new StreamableBoolean(true);
	private Map<String, TransportChannel> fileResponseChannelMap;

	public FileChannelHandler(TransportModule transport, FileTransportHandler fileHandler) {
		this.transport = transport;
		this.fileHandler = fileHandler;
		fileResponseChannelMap = new ConcurrentHashMap<String, TransportChannel>();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		Object m = e.getMessage();
		if (!(m instanceof ChannelBuffer)) {
			ctx.sendUpstream(e);
			return;
		}

		ChannelBuffer buffer = (ChannelBuffer) m;
		int readerIndex = buffer.readerIndex();
		byte type = buffer.getByte(readerIndex);

		if (!TransportOption.isTypeFile(type)) {
			ctx.sendUpstream(e);
			return;
		}

		logger.debug("file received[{}]>> {}", type, e);
		buffer.readByte();// type을 읽어서 버린다.
		int dataLength = buffer.readInt();

		int markedReaderIndex = buffer.readerIndex();
		int expectedIndexReader = markedReaderIndex + dataLength;
		DataInput wrappedStream = new ChannelBufferStreamInput(buffer, dataLength);

		long requestId = wrappedStream.readLong();
		byte status = wrappedStream.readByte();

		try {
			handleFileTransportRequest(ctx.getChannel(), wrappedStream, requestId);
		} catch (IOException e1) {
			logger.error("파일기록중 에러발생", e1);
			// 파일기록중 에러발생하면, 에러메시지 전송.
			final TransportChannel transportChannel = new TransportChannel(ctx.getChannel(), requestId);
			transportChannel.sendResponse(e1);

		} finally {
			wrappedStream.close();
		}

		if (buffer.readerIndex() != expectedIndexReader) {
			if (buffer.readerIndex() < expectedIndexReader) {
				logger.warn("Message not fully read (request) for [{}] and action [{}], resetting", requestId);
			} else {
				logger.warn("Message read past expected size (request) for [{}] and action [{}], resetting", requestId);
			}
			buffer.readerIndex(expectedIndexReader);
		}

	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelClosed(ctx, e);
	}

	private void handleFileTransportRequest(Channel channel, DataInput input, long requestId) throws IOException {
		logger.debug("File Handler >> {}, mapsize={}", this, fileResponseChannelMap.size());
		// seq(4) + [filepath(string) + filesize(long) + checksumCRC32(long)]+hashfilepath(string) + datalength(vint) + data
		int seq = input.readInt();
		String filePath = null;
		long fileSize = -1;
		long checksumCRC32 = 0;
		if (seq == 0) {
			filePath = input.readString();
			fileSize = input.readLong();
			checksumCRC32 = input.readLong();
			logger.debug("File Receive seq={}, filesize={}, crc={}, file={}", new Object[] { seq, fileSize, checksumCRC32, filePath });
		}
		String fileKey = input.readString();

		TransportChannel transportChannel = fileResponseChannelMap.get(fileKey);
		if (transportChannel == null) {
			transportChannel = new TransportChannel(channel, requestId);
			fileResponseChannelMap.put(fileKey, transportChannel);
		}
		boolean isDone = fileHandler.handleFile(seq, filePath, fileSize, checksumCRC32, fileKey, input);

		if (isDone) {
			fileResponseChannelMap.remove(fileKey);
			transportChannel.sendResponse(FILE_RECEIVE_DONE);
			logger.debug("파일수신 성공결과보냄.>>{}", fileKey);
		}
	}

}
