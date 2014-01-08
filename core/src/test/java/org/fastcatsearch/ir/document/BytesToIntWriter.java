package org.fastcatsearch.ir.document;

import java.io.IOException;

import org.fastcatsearch.ir.io.BytesBuffer;

public interface BytesToIntWriter {
	
	public int put(BytesBuffer buffer, int value) throws IOException;
	
	public int put(byte[] data, int offset, int length, int value) throws IOException;
}
