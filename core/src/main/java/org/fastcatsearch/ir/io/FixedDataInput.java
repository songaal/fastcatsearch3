package org.fastcatsearch.ir.io;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.common.IndexFileNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedDataInput implements SequencialDataInput {
	private static Logger logger = LoggerFactory.getLogger(FixedDataInput.class);
	private IndexInput dataInput;
	private int dataSize;
	
	private FixedDataInput(){ }
	
	public FixedDataInput(File dir, String fileName, int dataSize) throws IOException{
		File dataFile = new File(dir, fileName);
		dataInput = new BufferedFileInput(dataFile);
		this.dataSize = dataSize;
	}
	
	//범위체크하지 않음.
	@Override
	public boolean read(BytesRef bytesRef, long sequence) throws IOException{
		dataInput.seek(dataSize * sequence);

		if(bytesRef.bytes.length < dataSize){
			bytesRef.bytes = new byte[dataSize];
		}
		dataInput.readBytes(bytesRef.bytes, 0, dataSize);
		
		
//		String str = "";
//		for (int i = 0; i < dataSize; i++) {
//			str += (bytesRef.bytes[i] +",");
//		}
//		logger.debug("read fixed data [{}] seq[{}]", str, sequence);
		bytesRef.offset = 0;
		bytesRef.length = dataSize;
		return true;
	}
	
	@Override
	public void close() throws IOException{
		dataInput.close();
	}
	
	@Override
	public FixedDataInput clone(){
		FixedDataInput input = new FixedDataInput();
		input.dataInput = this.dataInput.clone();
		input.dataSize = this.dataSize;
		return input;
	}
}
