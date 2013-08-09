package org.fastcatsearch.ir.io;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.index.IndexWriteInfo;

public abstract class SequencialDataOutput extends IndexOutput {

	@Override
	public void seek(long pos) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() throws IOException {
	}

	@Override
	public void writeByte(byte b) throws IOException {
		throw new UnsupportedOperationException();
	}

	public abstract void getWriteInfo(List<IndexWriteInfo> writeInfoList);

}
