package org.fastcatsearch.ir.io;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IRFileName;

public class VariableDataInput implements SequencialDataInput {
	private IndexInput dataInput;
	private IndexInput positionInput;
	
	public VariableDataInput(File dir, String fileName) throws IOException{
		File dataFile = new File(dir, IRFileName.getSuffixFileName(fileName, "data"));
		File positionFile = new File(dir, IRFileName.getSuffixFileName(fileName, "position"));
		
		dataInput = new BufferedFileInput(dataFile);
		positionInput = new BufferedFileInput(positionFile);
	}
	
	//범위체크하지 않음.
	@Override
	public boolean read(BytesRef bytesRef, long sequence) throws IOException{
		positionInput.seek(sequence * IOUtil.SIZE_OF_LONG);
		dataInput.seek(positionInput.readLong());
		int size = dataInput.readVInt();
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
