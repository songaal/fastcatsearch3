package org.fastcatsearch.ir.io;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariableDataInput implements SequencialDataInput {
	private static Logger logger = LoggerFactory.getLogger(VariableDataInput.class);
	
	private IndexInput dataInput;
	private IndexInput positionInput;
	
	public VariableDataInput(File dir, String fileName) throws IOException{
		File dataFile = new File(dir, fileName);
		File positionFile = new File(dir, IndexFileNames.getPositionFileName(fileName));
		
		dataInput = new BufferedFileInput(dataFile);
		positionInput = new BufferedFileInput(positionFile);
	}
	
	private VariableDataInput(){ }
	
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
		
		String str = "";
		for (int i = 0; i < size; i++) {
			str += (bytesRef.bytes[i] +",");
		}
//		logger.debug("read variable data [{}] seq[{}]", str, sequence);
		
		bytesRef.offset = 0;
		bytesRef.length = size;
		return true;
	}
	
	@Override
	public void close() throws IOException{
		dataInput.close();
		positionInput.close();
	}
	
	@Override
	public VariableDataInput clone(){
		VariableDataInput input = new VariableDataInput();
		input.dataInput = this.dataInput.clone();
		input.positionInput = this.positionInput.clone();
		return input;
	}
}
