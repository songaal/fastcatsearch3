package org.fastcatsearch.transport.common;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.fastcatsearch.control.JobController;
import org.fastcatsearch.control.JobResult;
import org.fastcatsearch.ir.config.IRClassLoader;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.transport.ChannelBufferStreamInput;
import org.fastcatsearch.transport.TransportService;
import org.fastcatsearch.transport.TransportChannel;
import org.fastcatsearch.transport.TransportException;
import org.fastcatsearch.transport.TransportStatus;


public class MessageChannelHandler extends SimpleChannelUpstreamHandler {
	
	private static Logger logger = LoggerFactory.getLogger(MessageChannelHandler.class);
	private TransportService transport;
	
	public MessageChannelHandler(TransportService transport){
		this.transport = transport;
	}
	
	@Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
		
		Object m = e.getMessage();
        if (!(m instanceof ChannelBuffer)) {
            ctx.sendUpstream(e);
            return;
        }
        
        ChannelBuffer buffer = (ChannelBuffer) m;
        int readerIndex = buffer.readerIndex();
        readerIndex += 12;
        byte status = buffer.getByte(readerIndex);
        
        //파일전송이면 올려보낸다.
        if (TransportStatus.isFile(status)) {
        	ctx.sendUpstream(e);
            return;
        }
        
        int dataLength = buffer.readInt();
        long requestId = buffer.readLong();
		status = buffer.readByte();

        int markedReaderIndex = buffer.readerIndex();
        int expectedIndexReader = markedReaderIndex + dataLength;
        
        StreamInput wrappedStream = new ChannelBufferStreamInput(buffer, dataLength);

        if (TransportStatus.isRequest(status)) {
            handleRequest(ctx.getChannel(), wrappedStream, requestId);
            if (buffer.readerIndex() != expectedIndexReader) {
                if (buffer.readerIndex() < expectedIndexReader) {
                    logger.warn("Message not fully read (request) for [{}] and action [{}], resetting", requestId);
                } else {
                    logger.warn("Message read past expected size (request) for [{}] and action [{}], resetting", requestId);
                }
                buffer.readerIndex(expectedIndexReader);
            }
        } else {
        	
            if (TransportStatus.isError(status)) {
                handlerResponseError(wrappedStream, requestId);
            } else {
                handleResponse(wrappedStream, requestId);
            }
            
            if (buffer.readerIndex() != expectedIndexReader) {
                if (buffer.readerIndex() < expectedIndexReader) {
                    logger.warn("Message not fully read (response) for [{}] , error [{}], resetting", requestId, TransportStatus.isError(status));
                } else {
                    logger.warn("Message read past expected size (response) for [{}] , error [{}], resetting", requestId, TransportStatus.isError(status));
                }
                buffer.readerIndex(expectedIndexReader);
            }
        }
        
        wrappedStream.close();
        
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        super.channelClosed(ctx, e);
    }
    
    private void handleRequest(Channel channel, StreamInput input, long requestId) throws IOException {

        final TransportChannel transportChannel = new TransportChannel(channel, requestId);
        try {
        	String jobName = input.readString();
        	StreamableJob requestJob = (StreamableJob)IRClassLoader.getInstance().loadObject(jobName);
        	requestJob.readFrom(input);
        	transport.execute(new RequestHandler(requestJob, transportChannel));
        } catch (Exception e) {
            try {
                transportChannel.sendResponse(e);
            } catch (IOException e1) {
                logger.warn("Failed to send error message back to client", e);
                logger.warn("Actual Exception", e1);
            }
        }
    }
    
    private void handleResponse(StreamInput input, long requestId) {
        try {
        	String className = input.readString();
        	Streamable streamableResult = (Streamable) IRClassLoader.getInstance().loadObject(className);
        	streamableResult.readFrom(input);
        	
        	transport.resultReceived(requestId, streamableResult);
        } catch (Exception e) {
        	transport.exceptionReceived(requestId, e);
        }
    }

    private void handlerResponseError(StreamInput buffer, long requestId) {
        Throwable error;
        try {
        	ObjectInputStream ois = new ObjectInputStream(buffer);
            error = (Throwable) ois.readObject();
        } catch (Exception e) {
            error = new TransportException("Failed to deserialize exception response from stream", e);
        }
        transport.exceptionReceived(requestId, error);
    }
    
    class RequestHandler implements Runnable {
        private final StreamableJob requestJob;
        private final TransportChannel transportChannel;

        public RequestHandler(StreamableJob requestJob, TransportChannel transportChannel) {
            this.requestJob = requestJob;
            this.transportChannel = transportChannel;
        }

        @Override
        public void run() {
            try {
            	JobResult jobResult = JobController.getInstance().offer(requestJob);
            	Object result = jobResult.take();
            	if(result instanceof Streamable){
            		Streamable streamableResult = (Streamable) result;
            		transportChannel.sendResponse(streamableResult);
            	}else{
            		logger.error("###########################################");
            		logger.error("# JobResult가 Streamable 타입이 아니어서 전송불가! result >> {}", result);
            		logger.error("###########################################");
            	}
            		
            	
            } catch (Throwable e) {
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
