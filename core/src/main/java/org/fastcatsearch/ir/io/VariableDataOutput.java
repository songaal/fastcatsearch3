package org.fastcatsearch.ir.io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.common.IndexFileNames;
import org.fastcatsearch.ir.index.IndexWriteInfo;

public class VariableDataOutput extends SequencialDataOutput {
	private IndexOutput dataOutput;
	private IndexOutput positionOutput;
	
	public VariableDataOutput(File dir, String fileName) throws IOException{
		this(dir, fileName, false);
	}
	public VariableDataOutput(File dir, String fileName, boolean append) throws IOException{
		File dataFile = new File(dir, fileName);
		File positionFile = new File(dir, IndexFileNames.getPositionFileName(fileName));
		
		dataOutput = new BufferedFileOutput(dataFile, append);
		positionOutput = new BufferedFileOutput(positionFile, append);
	}
	
	@Override
	public void writeBytes(byte[] buffer, int offset, int length) throws IOException{
		positionOutput.writeLong(dataOutput.position());
		dataOutput.writeVInt(length);
		dataOutput.writeBytes(buffer, offset, length);
	}
	
	@Override
	public void writeBytes(BytesBuffer bytesBuffer) throws IOException{
		positionOutput.writeLong(dataOutput.position());
		dataOutput.writeVInt(bytesBuffer.length());
		dataOutput.writeBytes(bytesBuffer.bytes, bytesBuffer.offset, bytesBuffer.length());
	}
	
	@Override
	public void close() throws IOException{
		dataOutput.close();
		positionOutput.close();
	}
	@Override
	public void getWriteInfo(List<IndexWriteInfo> writeInfoList) {
		writeInfoList.add(dataOutput.getWriteInfo());
		writeInfoList.add(positionOutput.getWriteInfo());
	}
	
	@Override
	public void flush() throws IOException {
		dataOutput.flush();
		positionOutput.flush();
	}
	
	@Override
	public long position() throws IOException {
		throw new UnsupportedOperationException();
	}
	@Override
	public long length() throws IOException {
		throw new UnsupportedOperationException();
	}
	
}
