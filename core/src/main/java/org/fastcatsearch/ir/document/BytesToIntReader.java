package org.fastcatsearch.ir.document;

import java.io.IOException;

import org.fastcatsearch.ir.io.BytesBuffer;

public interface BytesToIntReader {
	
	public int get(BytesBuffer buffer) throws IOException;
	
	public int get(byte[] data, int offset, int length) throws IOException;
}
