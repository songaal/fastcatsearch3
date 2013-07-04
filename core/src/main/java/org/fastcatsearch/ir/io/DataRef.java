package org.fastcatsearch.ir.io;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;

public class DataRef {
	
	protected BytesRef bytesRef;
	protected int read;
	
	protected int count; //멀티밸류의 경우 여러번 읽어야하므로 갯수를 정해준다.
	
	public DataRef(){ }
		
	public DataRef(int size) {
		bytesRef = new BytesRef(size);
	}

	public void reset(int count){
		this.count = count;
		this.read = 0; //0으로 리셋.
	}
	
	public boolean next() throws IOException{
		if(read >= count) {
			return false;
		}
		//이미 ref에 데이터가 셋팅되어있으므로 읽지 않는다. 
		read++;
		return true;
	}
	
	public BytesRef bytesRef(){
		return bytesRef;
	}
	
	public int count(){
		return count;
	}
	
	public void skip() throws IOException{
		//do nothing
	}
	
	public NumberDataRef getNumberDataRef(Class<? extends Number> numberType){
		NumberDataRef obj = new NumberDataRef(numberType);
		obj.bytesRef = bytesRef;
		obj.read = read;
		return obj;
	}
}
