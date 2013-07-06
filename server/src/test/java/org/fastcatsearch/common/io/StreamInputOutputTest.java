package org.fastcatsearch.common.io;

import java.io.IOException;

import org.fastcatsearch.common.BytesReference;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamInputOutputTest {

	@Test
	public void test() throws IOException {
		String str = "This is common IO!";
		long l = 1024*1024*1024*1024;
		BytesStreamOutput output = new BytesStreamOutput();
		output.writeInt(4);
		output.writeString(str);
		output.writeLong(1024*1024*1024*1024);
		output.writeVInt(8);
		output.close();
		BytesReference ref = output.bytesReference();
		
		StreamInput input = new BytesStreamInput(ref);
		assertEquals(4, input.readInt());
		assertEquals(str, input.readString());
		assertEquals(l, input.readLong());
		assertEquals(8, input.readVInt());
	}

}
