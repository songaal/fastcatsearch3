package org.fastcatsearch.ir.io;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.junit.Test;

public class DataWriterReadTest {

	@Test
	public void test() throws IOException {
		BytesDataOutput output = new BytesDataOutput();
		output.writeInt(100);
		output.close();
		BytesRef bytesRef = output.bytesRef();
		System.out.println(bytesRef.offset);
		System.out.println(bytesRef.length());
		System.out.println(bytesRef.pos());
		
		int actual = IOUtil.readInt(bytesRef);
		assertEquals(100, actual);
		
	}

}
