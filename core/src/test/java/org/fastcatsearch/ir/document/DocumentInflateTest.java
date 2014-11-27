package org.fastcatsearch.ir.document;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.input.BoundedInputStream;
import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.IOUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DocumentInflateTest {
	
	@Test
	public void deflateAndInflateFileTest() throws IOException {
		
		int SIZE = 10 * 1000;
		File f = new File("/tmp/"+getClass().getName()+".tmp");
		System.out.println("f = "+ f.getAbsolutePath());
		DeflaterOutputStream dos = new DeflaterOutputStream(new FileOutputStream(f), new Deflater());
		
		for(int i = 0; i < SIZE; i++) {
			dos.write(i);
		}
		dos.close();
		
		InflaterInputStream iis = new InflaterInputStream(new FileInputStream(f), new Inflater());
		int i = 0;
		int exp = 0;
		int count = 0;
		while((i = iis.read()) != -1) {
			assertEquals(exp % 256, i);
			exp++;
			count++;
		}
		iis.close();
		
		assertEquals(SIZE, count);
		
		f.delete();
		
	}
	
	
	@Test
	public void deflateAndInflateFileTest2() throws IOException {
		
		int SIZE = 10 * 1000;
		File f = new File("/tmp/"+getClass().getName()+".tmp");
		System.out.println("f = "+ f.getAbsolutePath());
		
		byte[] data = new byte[SIZE];
		for(int i = 0; i < SIZE; i++) {
			data[i] = (byte) (i % 256);
		}
		Deflater compressor = new Deflater();
		compressor.setInput(data);
		compressor.finish();

		FileOutputStream fos = new FileOutputStream(f);
		
		
		byte[] workingBuffer = new byte[1024];
		int compressedDataLength = 0;
		while (!compressor.finished()) {
			int count = compressor.deflate(workingBuffer);
			fos.write(workingBuffer, 0, count);
			compressedDataLength += count;
		}
		fos.close();
		
		
		InflaterInputStream iis = new InflaterInputStream(new FileInputStream(f), new Inflater());
		int i = 0;
		int exp = 0;
		int count = 0;
		while((i = (byte) iis.read()) != -1) {
			assertEquals(data[exp], i);
			exp++;
			count++;
		}
		iis.close();
		
//		assertEquals(SIZE, count);
		
		f.delete();
		
	}
	
	@Test
	public void inflateInputStreamTest() throws IOException {
		
		File f = new File("/tmp/doc.2");
		
		InflaterInputStream iis = new InflaterInputStream(new FileInputStream(f), new Inflater());
		int i = 0, count = 0;
		while((i = iis.read()) != -1) {
			count++;
			System.out.println(i);
		}
		iis.close();
	}
	
	@Test
	public void inflaterFileTest() throws IOException {
		
		byte[] tmpBuf = new byte[10];
		byte[] workingBuffer = new byte[10];
		
		File f = new File("/tmp/doc.2");
		InputStream is = new BufferedFileInput(f);
		long len = f.length();
		BoundedInputStream fis = new BoundedInputStream(is, len);
		Inflater decompressor = new Inflater();
		decompressor.reset();
		try {
			while (!decompressor.finished()) {

				int count = 0;
				while((count = decompressor.inflate(workingBuffer)) == 0) {
					
					if (decompressor.finished() || decompressor.needsDictionary()) {
						// reached EOF error!
						System.out.println("error while read document.");
						break;
					}
					while (decompressor.needsInput()) {
						System.out.println("needsInput!");
						int n = fis.read(tmpBuf, 0, tmpBuf.length);
						if (n == -1) {
							throw new EOFException("Unexpected end of ZLIB input stream");
						}
						decompressor.setInput(tmpBuf, 0, n);
					}
				}
				
				System.out.println("count = "+ count);
//				if(count > 0) {
//					inflaterOutput.write(workingBuffer, 0, count);
//				}
				
			}
		} catch (DataFormatException e) {
			e.printStackTrace();
		} finally {
			decompressor.end();
			fis.close();
		}
	}
	
	@Test
	public void byteIntTest() {
		for(int i = -10; i <= 20; i++) {
			System.out.printf("%d > %d\n", i, (byte) i);
		}
	}
	
	private static Logger logger = LoggerFactory.getLogger(DocumentInflateTest.class);

	@Test
	public void testNegative() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtil.writeVInt(baos, 167);
		byte[] data = baos.toByteArray();
		logger.debug("{}", data);
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		
		int i = 0;
		while((i = is.read()) != -1) {
			System.out.println(i);
		}
	}
}
