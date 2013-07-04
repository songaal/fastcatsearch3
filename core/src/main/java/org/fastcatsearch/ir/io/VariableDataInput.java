package org.fastcatsearch.ir.io;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IRFileName;

public class VariableDataInput implements SequencialDataInput {
	private Input dataInput;
	private Input positionInput;
	
	public VariableDataInput(File dir, String fileName) throws IOException{
		File dataFile = new File(dir, IRFileName.getSuffixFileName(fileName, "data"));
		File positionFile = new File(dir, IRFileName.getSuffixFileName(fileName, "position"));
		
		dataInput = new BufferedFileInput(dataFile);
		positionInput = new BufferedFileInput(positionFile);
	}
	
	//범위체크하지 않음.
	@Override
	public boolean read(BytesRef bytesRef, long sequence) throws IOException{
		positionInput.position(sequence * IOUtil.SIZE_OF_LONG);
		dataInput.position(positionInput.readLong());
		int size = dataInput.readVariableByte();
		if(bytesRef == null || bytesRef.bytes.length < size){
			bytesRef.bytes = new byte[size];
		}
		dataInput.readBytes(bytesRef.bytes, 0, size);
		bytesRef.offset = 0;
		bytesRef.length = size;
		return true;
	}
	
	@Override
	public void close() throws IOException{
		dataInput.close();
		positionInput.close();
	}
}
