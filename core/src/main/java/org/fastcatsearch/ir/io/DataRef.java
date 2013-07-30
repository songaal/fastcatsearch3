package org.fastcatsearch.ir.io;

import java.io.IOException;

import org.apache.lucene.util.BytesRef;

public class DataRef {
	
	protected BytesRef bytesRef;
	protected int read;
	
	protected int count; //멀티밸류의 경우 여러번 읽어야하므로 갯수를 정해준다.
	
	private Class<? extends Number> numberType;
	
	public final static DataRef EMPTY_DATAREF = new DataRef();
	
	public DataRef(){ }
		
	public DataRef(int size) {
		bytesRef = new BytesRef(size);
	}

	//다시 읽을때사용.
	public void reset(){
		read = 0;
	}
	
	public void init(int count){
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
	
	public void setNumberType(Class<? extends Number> numberType){
		this.numberType = numberType;
	}
	
	public Number getNumber(){
		
		if(numberType == Integer.class){
			return bytesRef.toIntValue();
		}else if(numberType == Long.class){
			return bytesRef.toLongValue();
		}else if(numberType == Float.class){
			return Float.intBitsToFloat(bytesRef.toIntValue());
		}else if(numberType == Double.class){
			return Double.longBitsToDouble(bytesRef.toLongValue());
		}
		
		return null;
	}
	
	public int count(){
		return count;
	}
	
	public void skip() throws IOException{
		//do nothing
	}
	
}
