package org.fastcatsearch.ir.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.index.IndexWriteInfo;

public class FixedDataOutput extends SequencialDataOutput {
	private IndexOutput dataOutput;
	public FixedDataOutput(File dir, String fileName) throws IOException{
		this(dir, fileName, false);
	}
	public FixedDataOutput(File dir, String fileName, boolean append) throws IOException{
		File dataFile = new File(dir, fileName);
		dataOutput = new BufferedFileOutput(dataFile, append);
	}
	
	@Override
	public void writeBytes(byte[] buffer, int offset, int length) throws IOException{
		dataOutput.writeBytes(buffer, offset, length);
	}
	
	@Override
	public void writeBytes(BytesBuffer bytesBuffer) throws IOException{
		dataOutput.writeBytes(bytesBuffer.bytes, bytesBuffer.offset, bytesBuffer.length());
	}
	
	@Override
	public void close() throws IOException{
		dataOutput.close();
	}
	@Override
	public void getWriteInfo(List<IndexWriteInfo> writeInfoList) {
		writeInfoList.add(dataOutput.getWriteInfo());
	}
	
	@Override
	public void flush() throws IOException {
		dataOutput.flush();
	}
	
	@Override
	public long position() throws IOException {
		return dataOutput.position();
	}
	@Override
	public long length() throws IOException {
		return dataOutput.length();
	}
}
