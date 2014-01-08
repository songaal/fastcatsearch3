package org.fastcatsearch.ir.document;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.fastcatsearch.ir.util.Formatter;
import org.junit.Test;
/*
 * 테스트결과 최적셋팅은 
 * MEMORY_LIMIT = 64MB
 * indexInterval = 64
 * bucketSize = 64k
 * index check는 100,000 건 마다.
 * */
public class LargePrimaryKeyIndexWriterTest {

	@Test
	public void test() throws IOException {

		int MEMORY_LIMIT = 64 * 1024 * 1024;
		int COUNT = 1000000;
		File dir = new File("/Users/swsong/tmp");
		Random r = new Random(System.currentTimeMillis());
		int indexInterval = 64;
		int bucketSize = 64 * 1024;
		String filename = "pk.map";
		LargePrimaryKeyIndexWriter writer = new LargePrimaryKeyIndexWriter(dir, filename, indexInterval, bucketSize);

		byte[] data = new byte[64];
		
		
		for (int i = 0; i < COUNT; i++) {
			data[0] = (byte) r.nextInt(255);
			data[1] = (byte) r.nextInt(255);
			data[2] = (byte) r.nextInt(255);
			data[3] = (byte) r.nextInt(255);
			data[4] = (byte) r.nextInt(255);
			int prev = writer.put(data, 0, data.length, i);
			if(prev != -1){
				System.out.println("DUP delete >> " + prev);
			}
			if ((i+1) % 100000 == 0) {
				long memorySize = writer.checkWorkingMemorySize();
				System.out.println("check " + (i+1) + " mem "+Formatter.getFormatSize(memorySize) + " > " + Formatter.getFormatSize(MEMORY_LIMIT));
				if (memorySize > MEMORY_LIMIT) {
					writer.flush();
				}
			}
		}
		
		writer.close();
	}

	@Test
	public void testEmpty() throws IOException{

			File dir = new File("/Users/swsong/tmp");
			int indexInterval = 64;
			int bucketSize = 64 * 1024;
			String filename = "pk.map";
			LargePrimaryKeyIndexWriter writer = new LargePrimaryKeyIndexWriter(dir, filename, indexInterval, bucketSize);
			
			writer.close();
			
	}
	
	@Test
	public void testManual() throws IOException{

			File dir = new File("/Users/swsong/tmp");
			int indexInterval = 64;
			int bucketSize = 64 * 1024;
			String filename = "pk.map";
			LargePrimaryKeyIndexWriter writer = new LargePrimaryKeyIndexWriter(dir, filename, indexInterval, bucketSize);
			int value = 0;
			int prev = -1;
			prev = writeString(writer, "abc", value++);
			prev = writeString(writer, "abc", value++);
			prev = writeString(writer, "123", value++);
			prev = writeString(writer, "abc", value++);
			writer.flush();
			prev = writeString(writer, "abc", value++);
			prev = writeString(writer, "abc", value++);
			prev = writeString(writer, "123", value++);
			prev = writeString(writer, "abc", value++);
			
			prev = writeString(writer, "def", value++);
			prev = writeString(writer, "def", value++);
			
			writer.close();
			
	}

	private int writeString(LargePrimaryKeyIndexWriter writer, String string, int value) throws IOException {
		byte[] key = string.getBytes();
		return writer.put(key, 0, key.length, value);
	}
}
