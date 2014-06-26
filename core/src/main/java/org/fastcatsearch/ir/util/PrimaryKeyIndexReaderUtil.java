package org.fastcatsearch.ir.util;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.io.BufferedFileInput;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.ir.io.IndexInput;

public class PrimaryKeyIndexReaderUtil {
	
	private IndexInput input;
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		PrimaryKeyIndexReaderUtil util = new PrimaryKeyIndexReaderUtil(new File(args[0]), args[1]);
		util.print();
		util.close();
	}
	
	public PrimaryKeyIndexReaderUtil(File dir, String filename) throws IOException {
		input = new BufferedFileInput(dir, filename);
	}
	
	public void print() throws IOException {
		
		int count = input.readInt();
		System.out.println("total count = " + count);
		byte[] buf = new byte[8192];
		for(int i=0; i<count; i++) {
			int len = input.readVInt();
			input.read(buf,  0, len);
			int groupNo = input.readInt();
			char[] chars = IOUtil.readUChars(buf,  0, len);
			System.out.print("[");
			System.out.print(i);
			System.out.print("] ");
			System.out.print(new String(chars));
			System.out.print(" > ");
			System.out.println(groupNo);
		}
			
	}
	
	public void close() throws IOException {
		if(input != null) {
			input.close();
		}
	}
}
