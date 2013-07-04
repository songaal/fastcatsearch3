package org.fastcatsearch.ir.io;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;

public interface SequencialDataOutput {
	public void write(byte[] buffer, int offset, int length) throws IOException;
	public void write(BytesRef bytesRef) throws IOException;
	public void close() throws IOException;
}
