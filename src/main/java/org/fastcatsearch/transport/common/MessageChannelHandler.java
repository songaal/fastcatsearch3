package org.fastcatsearch.transport.common;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.fastcatsearch.control.JobExecutor;
import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.ir.config.IRClassLoader;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.transport.ChannelBufferStreamInput;
import org.fastcatsearch.transport.TransportModule;
import org.fastcatsearch.transport.TransportChannel;
import org.fastcatsearch.transport.TransportException;
import org.fastcatsearch.transport.TransportOption;


public class MessageChannelHandler extends SimpleChannelUpstreamHandler {
	
	private static Logger logger = LoggerFactory.getLogger(MessageChannelHandler.class);
	private TransportModule transport;
	private JobExecutor jobExecutor;
	
	public MessageChannelHandler(TransportModule transport, JobExecutor jobExecutor){
		this.transport = transport;
		this.jobExecutor = jobExecutor;
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
        byte type = buffer.getByte(readerIndex);
        
        //타입이 메시지가 아니면 올려보낸다.
        if (!TransportOption.isTypeMessage(type)) {
        	ctx.sendUpstream(e);
            return;
        }
        
        logger.debug("message received[{}]>> {}", type, e);
        buffer.readByte();//type을 읽어서 버린다.
        int dataLength = buffer.readInt();
        
        int markedReaderIndex = buffer.readerIndex();
        int expectedIndexReader = markedReaderIndex + dataLength;
        StreamInput wrappedStream = new ChannelBufferStreamInput(buffer, dataLength);
        
        long requestId = wrappedStream.readLong();
		byte status = wrappedStream.readByte();
		logger.debug("message status[{}]", status);
		logger.debug("## readIndex={}, writerIndex={}", buffer.readerIndex(), buffer.writerIndex());
		//logger.debug("## readString={}", wrappedStream.readString());
        if (TransportOption.isRequest(status)) {
//        	int readTo = wrappedStream.available();
//        	for (int i = 0; i < readTo; i++) {
//        		wrappedStream.read();
//			}
            handleRequest(ctx.getChannel(), wrappedStream, requestId);
            logger.debug("buffer.readerIndex()={}, expectedIndexReader={}", buffer.readerIndex(), expectedIndexReader);
            if (buffer.readerIndex() != expectedIndexReader) {
                if (buffer.readerIndex() < expectedIndexReader) {
                    logger.warn("Message not fully read (request) for [{}] and action [{}], resetting", requestId);
                } else {
                    logger.warn("Message read past expected size (request) for [{}] and action [{}], resetting", requestId);
                }
                buffer.readerIndex(expectedIndexReader);
            }
        } else {
        	
            if (TransportOption.isError(status)) {
                handlerResponseError(wrappedStream, requestId);
            } else {
                handleResponse(wrappedStream, requestId);
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
        super.channelClosed(ctx, e);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    	logger.error("에러발생 >> {}", e.getCause().getMessage());
    }
    private void handleRequest(Channel channel, StreamInput input, long requestId) throws IOException {
    	logger.debug("handleRequest ");
        final TransportChannel transportChannel = new TransportChannel(channel, requestId);
        try {
        	String jobName = input.readString();
        	logger.debug("#### READ job = {}", jobName);
        	StreamableJob requestJob = (StreamableJob)IRClassLoader.getInstance().loadObject(jobName);
        	requestJob.readFrom(input);
        	
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
    
    private void handleResponse(StreamInput input, long requestId) {
        try {
        	String className = input.readString();
        	Streamable streamableResult = (Streamable) IRClassLoader.getInstance().loadObject(className);
        	streamableResult.readFrom(input);
        	logger.debug("## Response-{} >> {}", requestId, streamableResult.toString());
        	
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
            logger.debug("에러도착 Response-{} >> {}", requestId, error.getMessage());
        } catch (Exception e) {
            error = new TransportException("Failed to deserialize exception response from stream", e);
        }
        transport.exceptionReceived(requestId, error);
    }
    
    class RequestHandler implements Runnable {
        private final StreamableJob requestJob;
        private final TransportChannel transportChannel;

        public RequestHandler(StreamableJob requestJob, TransportChannel transportChannel) {
        	logger.debug("Request Job >> {}", requestJob.getClass().getName());
            this.requestJob = requestJob;
            this.transportChannel = transportChannel;
        }

        @Override
        public void run() {
            try {
            	ResultFuture jobResult = jobExecutor.offer(requestJob);
            	Object result = jobResult.take();
            	logger.debug("Request Job Result >> {}", result);
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
