package org.fastcatsearch.common.io;

import static org.junit.Assert.*;

import java.io.IOException;

import org.fastcatsearch.transport.ChannelBufferStreamInput;
import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Test;

public class StreamAndChannelBufferTest {

	// StreamOutput으로 기록한 데이터를 ChannelBuffer 방식으로 읽어들였을때 문제가 없는지 테스트.
	@Test
	public void test1() throws IOException {
		long l = 15634240;
		BytesStreamOutput stream = new BytesStreamOutput();
		stream.writeInt(4);
		stream.writeLong(l);
		stream.close();
		ChannelBuffer buffer = stream.bytesReference().toChannelBuffer();
		assertEquals(4, buffer.readInt());
		assertEquals(l, buffer.readLong());
	}
	
	@Test
	public void testLong() throws IOException {
		long st = System.currentTimeMillis();
		for(long l = 0; l< Long.MAX_VALUE; l++){
//			BytesStreamOutput stream = CachedStreamOutput.popEntry().bytes();
			BytesStreamOutput stream = new BytesStreamOutput(8);
			stream.writeLong(l);
			stream.close();
			ChannelBuffer buffer = stream.bytesReference().toChannelBuffer();
			assertEquals(l, buffer.readLong());
			if((l % 100000000) == 0){
				System.out.println(l+".. / "+Long.MAX_VALUE +" / "+(l*100.0 / (Long.MAX_VALUE*1.0))+"%");
				System.out.println("time="+ (System.currentTimeMillis() - st)+"ms");
				st = System.currentTimeMillis();
			}
		}
	}
	
	// StreamOutput으로 기록한 데이터를 ChannelBuffer로 변경후 전송했다는 가정하에 
	// ChannelBufferStreamInput로 래핑해서 다시 읽어들였을때 문제가 없는지 테스트.
	@Test
	public void test2() throws IOException {
		String str = "This is common IO!";
		long l = 15634240;
		BytesStreamOutput stream = new BytesStreamOutput();
		stream.writeInt(4);
		stream.writeString(str);
		stream.writeLong(l);
		stream.writeVInt(8);
		stream.close();
		int dataLength = stream.length();
		ChannelBuffer buffer = stream.bytesReference().toChannelBuffer();
		//전송후 받아서 다시 읽는다.
		StreamInput wrappedStream = new ChannelBufferStreamInput(buffer, dataLength);
		
		assertEquals(4, wrappedStream.readInt());
		assertEquals(str, wrappedStream.readString());
		assertEquals(l, wrappedStream.readLong());
		assertEquals(8, wrappedStream.readVInt());
	}
	
	@Test
	public void testLong2() throws IOException {
		long st = System.currentTimeMillis();
		for(long l = 0; l< Long.MAX_VALUE; l++){
//			BytesStreamOutput stream = CachedStreamOutput.popEntry().bytes();
			BytesStreamOutput stream = new BytesStreamOutput(8);
			stream.writeLong(l);
			stream.close();
			int dataLength = stream.length();
			ChannelBuffer buffer = stream.bytesReference().toChannelBuffer();
			//전송후 받아서 다시 읽는다.
			StreamInput wrappedStream = new ChannelBufferStreamInput(buffer, dataLength);
			assertEquals(l, wrappedStream.readLong());
			if((l % 100000000) == 0){
				System.out.println(l+".. / "+Long.MAX_VALUE +" / "+(l*100.0 / (Long.MAX_VALUE*1.0))+"%");
				System.out.println("time="+ (System.currentTimeMillis() - st)+"ms");
				st = System.currentTimeMillis();
			}
		}
	}

}
