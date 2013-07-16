package org.fastcatsearch.ir.io;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.junit.Test;

public class DataIOTest {

	@Test
	public void test() throws IOException {
		String str = "abcdefghijk1234567한글입니다.1111日本語 ( にほんご";
		System.out.println(str);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutput output = new OutputStreamDataOutput(baos);
		output.writeString(str);
		output.flush();
		
		byte[] buffer = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		DataInput input = new InputStreamDataInput(bais);
		String actual = input.readString();
		
		System.out.println(actual);
		assertTrue(actual.equals(str));
	}

}
