package org.fastcatsearch.ir.io;

import java.io.IOException;

public interface SequencialDataOutput {
	public void write(byte[] buffer, int offset, int length) throws IOException;
	public void write(BytesBuffer bytesBuffer) throws IOException;
	public void close() throws IOException;
}
