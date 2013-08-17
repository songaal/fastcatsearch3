package org.fastcatsearch.ir.io;

import java.io.ByteArrayOutputStream;

import org.apache.lucene.util.BytesRef;

public class ByteRefArrayOutputStream extends ByteArrayOutputStream {
	
	
	public ByteRefArrayOutputStream() {
		super();
	}
	
	public ByteRefArrayOutputStream(int size) {
		super(size);
	}
	
	public BytesRef getBytesRef(){
		return new BytesRef(buf, 0, count);
	}
	
	public byte[] array(){
		return buf;
	}
	
	public int length(){
		return count;
	}
}
