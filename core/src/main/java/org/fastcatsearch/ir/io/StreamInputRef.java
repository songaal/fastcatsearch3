package org.fastcatsearch.ir.io;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
/**
 * input에서 dataSize 만큼을 읽어서 byteRef로 리턴해주는 클래스. 
 * */
public class StreamInputRef extends DataRef {
	protected IndexInput input;
	protected int dataSize;
	protected long resetPosition;
	
	public StreamInputRef(){ }
	
	public StreamInputRef(IndexInput input, int dataSize) {
		this.input = input;
		this.dataSize = dataSize;
		bytesRef = new BytesRef(dataSize);
		count = 1; //기본 1.
	}
	
	@Override
	public void reset() {
		try {
			input.seek(resetPosition);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		read = 0;
	}
	
	@Override
	public void init(int count){
		this.count = count;
		resetPosition = input.position();
		this.read = 0; //0으로 리셋.
	}
	
	@Override
	public boolean next() throws IOException{
		if(read >= count) {
			return false;
		}
//		logger.debug("next seek {} read {}", input.position(), dataSize);
		input.readBytes(bytesRef.bytes, 0, dataSize);
		read++;
		return true;
	}
	
	@Override
	public void skip() throws IOException{
		input.seek(input.position() + dataSize);
		read++;
	}
	
	
}
