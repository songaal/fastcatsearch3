package org.fastcatsearch.ir.io;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IRFileName;

public class FixedDataOutput implements SequencialDataOutput {
	private Output dataOutput;
	
	public FixedDataOutput(File dir, String fileName) throws IOException{
		this(dir, fileName, false);
	}
	public FixedDataOutput(File dir, String fileName, boolean append) throws IOException{
		File dataFile = new File(dir, IRFileName.getSuffixFileName(fileName, "data"));
		dataOutput = new BufferedFileOutput(dataFile, append);
	}
	
	@Override
	public void write(byte[] buffer, int offset, int length) throws IOException{
		dataOutput.writeVariableByte(length);
		dataOutput.writeBytes(buffer, offset, length);
	}
	
	@Override
	public void write(BytesRef bytesRef) throws IOException{
		dataOutput.writeVariableByte(bytesRef.length);
		dataOutput.writeBytes(bytesRef.bytes, bytesRef.offset, bytesRef.length);
	}
	
	@Override
	public void close() throws IOException{
		dataOutput.close();
	}
}
