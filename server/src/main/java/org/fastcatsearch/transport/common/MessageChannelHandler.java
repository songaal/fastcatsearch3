package org.fastcatsearch.transport.common;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.transport.ChannelBufferStreamInput;
import org.fastcatsearch.transport.TransportChannel;
import org.fastcatsearch.transport.TransportException;
import org.fastcatsearch.transport.TransportModule;
import org.fastcatsearch.transport.TransportOption;
import org.fastcatsearch.transport.vo.StreamableThrowable;
import org.fastcatsearch.util.DynamicClassLoader;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageChannelHandler extends SimpleChannelUpstreamHandler {

	private static Logger logger = LoggerFactory.getLogger(MessageChannelHandler.class);
	private Environment environment;
	private TransportModule transport;
	private JobExecutor jobExecutor;
	private String id;

	public MessageChannelHandler(String id, Environment environment, TransportModule transport, JobExecutor jobExecutor) {
		this.id = id;
		this.environment = environment;
		this.transport = transport;
		this.jobExecutor = jobExecutor;
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

		// 타입이 메시지가 아니면 올려보낸다.
		if (!TransportOption.isTypeMessage(type)) {
			ctx.sendUpstream(e);
			return;
		}

		// logger.debug("message received[{}]>> {}", type, e);
		buffer.readByte();// type을 읽어서 버린다.
		int dataLength = buffer.readInt();

		int markedReaderIndex = buffer.readerIndex();
		int expectedIndexReader = markedReaderIndex + dataLength;
		DataInput wrappedStream = new ChannelBufferStreamInput(buffer, dataLength);

		long requestId = wrappedStream.readLong();
		byte status = wrappedStream.readByte();
		// logger.debug("message status[{}]", status);
		// logger.debug("## readIndex={}, writerIndex={}", buffer.readerIndex(), buffer.writerIndex());
		// logger.debug("## readString={}", wrappedStream.readString());
		if (TransportOption.isRequest(status)) {
			// int readTo = wrappedStream.available();
			// for (int i = 0; i < readTo; i++) {
			// wrappedStream.read();
			// }
			handleRequest(ctx.getChannel(), wrappedStream, requestId);
			// logger.debug("buffer.readerIndex()={}, expectedIndexReader={}", buffer.readerIndex(), expectedIndexReader);
			if (buffer.readerIndex() != expectedIndexReader) {
				if (buffer.readerIndex() < expectedIndexReader) {
					// logger.warn("Message not fully read (request) for [{}] and action [{}], resetting", requestId);
				} else {
					// logger.warn("Message read past expected size (request) for [{}] and action [{}], resetting", requestId);
				}
				buffer.readerIndex(expectedIndexReader);
			}
		} else {
			// logger.debug("# status = {}", status);
			if (TransportOption.isError(status)) {
				// logger.debug("# status isError");
				handlerErrorResponse(wrappedStream, requestId);
			} else if (TransportOption.isResponseObject(status)) {
				// logger.debug("# status isResponseObject");
				handleObjectResponse(wrappedStream, requestId);
			} else {
				// logger.debug("# status isResponse streamable");
				handleStreamableResponse(wrappedStream, requestId);
			}

			if (buffer.readerIndex() != expectedIndexReader) {
				if (buffer.readerIndex() < expectedIndexReader) {
					logger.warn("Message not fully read (response) for [{}] , error [{}], resetting", requestId, TransportOption.isError(status));
				} else {
					logger.warn("Message read past expected size (response) for [{}] , error [{}], resetting", requestId, TransportOption.isError(status));
				}
				buffer.readerIndex(expectedIndexReader);
			}
		}

		wrappedStream.close();

	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.debug("CLOSED! {} {}", id, ctx.getChannel(), ctx.getChannel());
		super.channelClosed(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		logger.error("에러발생 >> {}", e.getCause().getMessage());
	}

	private void handleRequest(Channel channel, DataInput input, long requestId) throws IOException {
		// logger.debug("handleRequest ");
		final TransportChannel transportChannel = new TransportChannel(channel, requestId);
		try {
			String jobName = input.readString();
			boolean isNoResult = input.readBoolean();
			boolean isScheduled = input.readBoolean();
			 logger.debug("#### READ job = {}", jobName);
			Job requestJob = DynamicClassLoader.loadObject(jobName, Job.class);
			requestJob.setEnvironment(environment);
			if (isNoResult) {
				requestJob.setNoResult();
			}
			requestJob.setScheduled(isScheduled);

			if (requestJob instanceof Streamable) {
				((Streamable) requestJob).readFrom(input);
			}

			transport.execute(new RequestHandler(requestJob, transportChannel));
		} catch (Exception e) {
			logger.error("", e);
			try {
				transportChannel.sendResponse(e);
			} catch (IOException e1) {
				logger.warn("Failed to send error message back to client", e);
				logger.warn("Actual Exception", e1);
			}
		}
	}

	private void handleStreamableResponse(DataInput input, long requestId) {
		try {
			String className = input.readString();
			Streamable streamableResult = DynamicClassLoader.loadObject(className, Streamable.class);
			streamableResult.readFrom(input);
			// logger.debug("## Response-{} >> {}", requestId, streamableResult.toString());

			transport.resultReceived(requestId, streamableResult);
		} catch (Exception e) {
			StreamableThrowable streamableThrowable = new StreamableThrowable(e);
			transport.exceptionReceived(requestId, streamableThrowable);
		}
	}

	private void handleObjectResponse(DataInput input, long requestId) {
		try {
			Object response = input.readGenericValue();
			// logger.debug("## Response-{} >> {}", requestId, response);

			transport.resultReceived(requestId, response);
		} catch (Exception e) {
			StreamableThrowable streamableThrowable = new StreamableThrowable(e);
			transport.exceptionReceived(requestId, streamableThrowable);
		}
	}

	private void handlerErrorResponse(DataInput buffer, long requestId) {
		StreamableThrowable streamableThrowable = null;
		try {
			streamableThrowable = new StreamableThrowable();
			streamableThrowable.readFrom(buffer);
			logger.debug("에러도착 Response-{} >> {}", requestId, streamableThrowable.getThrowable());
		} catch (Exception e) {
			streamableThrowable = new StreamableThrowable(new TransportException("Failed to deserialize exception response from stream", e));
		}
		transport.exceptionReceived(requestId, streamableThrowable);
	}

	class RequestHandler implements Runnable {
		private final Job job;
		private final TransportChannel transportChannel;

		public RequestHandler(Job job, TransportChannel transportChannel) {
			// logger.debug("Request Job >> {}", job.getClass().getName());
			this.job = job;
			this.transportChannel = transportChannel;
		}

		@Override
		public void run() {
			try {
				if (job.isNoResult()) {
					jobExecutor.offer(job);
				} else {
					ResultFuture resultFuture = jobExecutor.offer(job);
                    long timeout = job.getTimeout();
                    Object obj = resultFuture.take();
					// logger.debug("## RequestHandler {} result >> {}", job.getClass().getSimpleName(), obj);
					if (obj instanceof Streamable) {
						Streamable result = (Streamable) obj;
						transportChannel.sendResponse(result);
					} else if (obj instanceof Throwable) {
						throw (Throwable) obj;
					} else {
						// 전송된 job의 결과가 streamable이 아니라면 어떻게 할까?
						transportChannel.sendResponse(obj);
					}
				}
				// logger.debug("Request Job Result >> {}", obj);
			} catch (Throwable e) {
				logger.error("Fail to write response message", e);
				// we can only send a response transport is started....
				try {
					transportChannel.sendResponse(e);
				} catch (IOException e1) {
					logger.warn("Failed to send error message back to client", e1);
					logger.warn("Actual Exception", e);
				}
			}
		}
	}

}
