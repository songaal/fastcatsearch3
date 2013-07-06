package org.fastcatsearch.ir.io;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;
/**
 * input에서 dataSize 만큼을 읽어서 byteRef로 리턴해주는 클래스. 
 * */
public class StreamInputRef extends DataRef {
	protected IndexInput input;
	protected int dataSize;
	
	public StreamInputRef(){ }
	
	public StreamInputRef(IndexInput input, int dataSize) {
		this.input = input;
		this.dataSize = dataSize;
		bytesRef = new BytesRef(dataSize);
		count = 1; //기본 1.
	}
	
	@Override
	public boolean next() throws IOException{
		if(read >= count) {
			return false;
		}
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
