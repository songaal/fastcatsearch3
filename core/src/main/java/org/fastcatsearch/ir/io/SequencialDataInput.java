package org.fastcatsearch.ir.io;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;

public interface SequencialDataInput extends Cloneable {
	public boolean read(BytesRef bytesRef, long sequence) throws IOException;
	public void close() throws IOException;
	public SequencialDataInput clone();
}
