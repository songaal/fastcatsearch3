package org.fastcatsearch.ir.io;

import java.io.ByteArrayInputStream;

import org.apache.lucene.util.BytesRef;

public class ByteRefArrayInputStream extends ByteArrayInputStream {

	public ByteRefArrayInputStream(byte[] buf) {
		super(buf);
	}
	
	public ByteRefArrayInputStream(byte[] buf, int offset, int length) {
		super(buf, offset, length);
	}
	
	public BytesRef getBytesRef(){
		return new BytesRef(buf, 0, pos);
	}

}
