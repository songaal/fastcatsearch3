package org.fastcatsearch.ir.io;

import java.io.File;
import java.io.IOException;

import org.fastcatsearch.ir.common.IRFileName;

public class VariableDataOutput implements SequencialDataOutput {
	private IndexOutput dataOutput;
	private IndexOutput positionOutput;
	
	public VariableDataOutput(File dir, String fileName) throws IOException{
		this(dir, fileName, false);
	}
	public VariableDataOutput(File dir, String fileName, boolean append) throws IOException{
		File dataFile = new File(dir, IRFileName.getSuffixFileName(fileName, "data"));
		File positionFile = new File(dir, IRFileName.getSuffixFileName(fileName, "position"));
		
		dataOutput = new BufferedFileOutput(dataFile, append);
		positionOutput = new BufferedFileOutput(positionFile, append);
	}
	
	@Override
	public void write(byte[] buffer, int offset, int length) throws IOException{
		positionOutput.writeLong(dataOutput.position());
		dataOutput.writeVInt(length);
		dataOutput.writeBytes(buffer, offset, length);
	}
	
	@Override
	public void write(BytesBuffer bytesBuffer) throws IOException{
		positionOutput.writeLong(dataOutput.position());
		dataOutput.writeVInt(bytesBuffer.length);
		dataOutput.writeBytes(bytesBuffer.bytes, bytesBuffer.offset, bytesBuffer.length);
	}
	
	@Override
	public void close() throws IOException{
		dataOutput.close();
		positionOutput.close();
	}
}
