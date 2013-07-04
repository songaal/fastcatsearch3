package org.fastcatsearch.transport;

import java.io.IOException;
import java.io.StreamCorruptedException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProtocol {
	
	private static Logger logger = LoggerFactory.getLogger(MessageProtocol.class);
	//MESSAGE_PREFIX
	public static final int HEADER_SIZE = 2 + 1 + 4 + 8 + 1; //HEADER(2) + type(1) + data-length(4) + requestId(8) + status(1)
	
	private static byte[] handShakeRequestData = new byte[]{'R','E','Q','?'};
	private static byte[] handShakeEstablishedData = new byte[]{'F','I','N','E'};
	
	private static byte[] HEADER = new byte[]{'F','S'}; //FastcatSearch
	private static byte[] EOM = new byte[]{'F','S','E','M'}; 
	
	private static ChannelBuffer handshakeRequestBuffer;
	private static ChannelBuffer handshakeEstablishedBuffer;
	
//	static{
//		handshakeRequestBuffer = ChannelBuffers.buffer(HEADER_SIZE + handShakeRequestData.length);
//		writeHeader(handshakeRequestBuffer, handShakeRequestData.length);
//		
//		handshakeEstablishedBuffer = ChannelBuffers.buffer(HEADER_SIZE + handShakeEstablishedData.length);
//		writeHeader(handshakeEstablishedBuffer, handShakeEstablishedData.length);
//	}
	
//	public static void writeHeader(ChannelBuffer buffer, int dataLength){
//		buffer.writeBytes(HEADER);
//		buffer.writeInt(dataLength);
//		buffer.writeByte(1);
//		buffer.writeByte(0);
//	}
	
//	public static ChannelBuffer newHandshakeRequest(){
//		ChannelBuffer buffer = handshakeRequestBuffer.copy();
//		writeHeader(buffer, handShakeRequestData.length);
//		buffer.writeBytes(handShakeRequestData);
//		return buffer;
//	}
//	public static ChannelBuffer newHandshakeEstablished(){
//		ChannelBuffer buffer = handshakeEstablishedBuffer.copy();
//		writeHeader(buffer, handShakeEstablishedData.length);
//		buffer.writeBytes(handShakeEstablishedData);
//		return buffer;
//	}
	
	public static void main(String[] args) {
		for (int i = 0; i < HEADER.length; i++) {
			System.out.println(HEADER[i]+", "+(char)HEADER[i]);
		}
	}
	
//	public static boolean readHeader(StreamInput input){
//		byte[] buf = new byte[HEADER.length];
//		if(readFully(input, buf, buf.length) != HEADER.length){
//			return false;
//		}
//		
//		if(!isValidHeader(buf)){
//			logger.error("메시지 헤더가 올바르지 않습니다. {}", (char)buf[0]+", "+(char)buf[1]+", "+(char)buf[2]+", "+(char)buf[3]);
//			return false;
//		}
//		
//		return true;
//	}
	
	public static void writeHeader(ChannelBuffer buffer, byte type, long requestId, byte status) {
		int index = buffer.readerIndex();
        buffer.setByte(index, HEADER[0]);
        index += 1;
        buffer.setByte(index, HEADER[1]);
        index += 1;
        buffer.setByte(index, type);
        index += 1;
        buffer.setInt(index, buffer.readableBytes() - 7);
        index += 4;
        buffer.setLong(index, requestId);
        index += 8;
        buffer.setByte(index, status);
	}
	
	private static boolean isValidHeader(byte[] buf) {
		for (int i = 0; i < HEADER.length; i++) {
			if(HEADER[i] != buf[i]){
				return false;
			}
		}
		return true;
	}
	
	private static boolean isValidEOM(byte[] buf) {
		for (int i = 0; i < EOM.length; i++) {
			if(EOM[i] != buf[i]){
				return false;
			}
		}
		return true;
	}

//	private static int readFully(StreamInput input, byte[] buf, int len){
//		int nread = 0;
//		
//		try {
//			while(nread < len){
//				nread += input.readBytes(buf, 0, len);
//			}
//		} catch (IOException e) {
//			logger.error("데이터를 읽는중 에러발생.", e);
//		}
//		
//		return nread;
//	}

//	public static Message readMessageClass(Input input) {
//		try {
//			int len = input.readVariableByte();
//			char[] className = input.readAChars(len);
//			return (Message)IRClassLoader.getInstance().loadObject(new String(className));
//		} catch (IOException e) {
//			logger.error("Input 에러.", e);
//		}
//		return null;
//	}

//	public static void writeMessageClass(Output output, Message message) {
//		try {
//			char[] className = message.getClass().getName().toCharArray();
//			output.writeVariableByte(className.length);
//			output.writeAChars(className, 0, className.length);
//		} catch (IOException e) {
//			logger.error("Output 에러.", e);
//		}
//	}

//	public static boolean writeEndOfMessage(Output output) {
//		try {
//			output.writeBytes(EOM);
//		} catch (IOException e) {
//			logger.error("EOM기록중 에러발생.", e);
//			return false;
//		}
//		return true;
//	}

	public static boolean writeHandshake1(ChannelBuffer output){
		logger.debug("HandShake1 기록!");
		output.writeBytes(handShakeRequestData);
		logger.debug("HandShake1 기록완료!");
		return true;
	}
	
	public static boolean writeHandshake2(ChannelBuffer output){
		logger.debug("HandShake2 기록!");
		output.writeBytes(handShakeEstablishedData);
		logger.debug("HandShake2 기록완료!");
		return true;	
	}
	
	public static boolean readHandshake1(ChannelBuffer input){
		logger.debug("HandShake1 읽기!");
		for (int i = 0; i < handShakeRequestData.length; i++) {
			if(handShakeRequestData[i] != input.readByte()){
				return false;
			}
		}
		logger.debug("HandShake1 읽기완료!");
		return true;
	}
	
	public static boolean readHandshake2(ChannelBuffer input){
		logger.debug("HandShake2 읽기!");
		for (int i = 0; i < handShakeEstablishedData.length; i++) {
			if(handShakeEstablishedData[i] != input.readByte()){
				return false;
			}
		}
		logger.debug("HandShake2 읽기완료!");
		return true;
	}

//	public static boolean readEndOfMessage(Input input) {
//		byte[] buf = new byte[EOM.length];
//		if(readFully(input, buf, buf.length) != EOM.length){
//			return false;
//		}
//		
//		if(!isValidEOM(buf)){
//			logger.error("메시지 EOM이 올바르지 않습니다.{}", (char)buf[0]+", "+(char)buf[1]+", "+(char)buf[2]+", "+(char)buf[3]);
//			return false;
//		}
//		
//		return true;
//		
//	}

	public static boolean isBufferReady(ChannelBuffer buffer, int readerIndex) throws IOException {
		//prefix + version + status + dataLength
		
		int readAhead = 0;
//		logger.debug("readerIndex = {}, buffer={}", readerIndex, buffer);
//		logger.debug("{}:{}", buffer.getByte(readerIndex), HEADER[0]);
//        logger.debug("{}:{}", buffer.getByte(readerIndex+1), HEADER[1]);
        
        
		if (buffer.getByte(readerIndex) != HEADER[0]
				|| buffer.getByte(readerIndex + 1) != HEADER[1]) {
			throw new StreamCorruptedException("Invalid protocol data format");
		}
		readAhead += 2;
		
		int type = buffer.getByte(readerIndex + readAhead);
		readAhead += 1;
		
		int dataLength = buffer.getInt(readerIndex + readAhead);
		readAhead += 4;
		
		if (dataLength <= 0) {
			throw new StreamCorruptedException("Invalid data length : "+dataLength);
		}
		
		
		long requestId = buffer.getLong(readerIndex + readAhead);
		readAhead += 8;
		
		int status = buffer.getByte(readerIndex + readAhead);
		readAhead += 1;
		

		// jvm max heap사이즈를 고려하여 max사이즈를 넘지 않도록한다.
//		if (dataLength > MAX_ALLOWED_SIZE) {
//			throw new Exception
//		}

		if (buffer.readableBytes() < 7 + dataLength) {
//			logger.debug("버퍼가 아직은 모자람.{} < {}", buffer.readableBytes(), dataLength + 7);
			return false;
		}
		
//		logger.debug("버퍼가 충분함.{} : {}", buffer.readableBytes(), dataLength + 7);
		return true;
	}

	
}
