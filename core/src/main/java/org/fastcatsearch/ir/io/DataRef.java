package org.fastcatsearch.ir.io;

import java.io.IOException;
import java.util.Date;

import org.apache.lucene.util.BytesRef;
import org.fastcatsearch.ir.settings.FieldSetting.Type;
import org.fastcatsearch.ir.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataRef {
	protected static Logger logger = LoggerFactory.getLogger(DataRef.class);
	
	protected BytesRef bytesRef;
	protected int read;
	
	protected int count; //멀티밸류의 경우 여러번 읽어야하므로 갯수를 정해준다.
	
	private Type type;
	
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
	
	public void setType(Type type) {
		this.type = type;
		
	}
	
	public Object getValue(){
		
		if(type == Type.INT){
			return bytesRef.toIntValue();
		}else if(type == Type.LONG){
			return bytesRef.toLongValue();
		}else if(type == Type.FLOAT){
			return Float.intBitsToFloat(bytesRef.toIntValue());
		}else if(type == Type.DOUBLE){
			return Double.longBitsToDouble(bytesRef.toLongValue());
		}else if(type == Type.ASTRING) {
			return new String(bytesRef.bytes);
		}else if(type == Type.STRING) {
			return new String(bytesRef.toUCharArray());
        }else if(type == Type.DATETIME) {
            return Formatter.formatDate(new Date(bytesRef.toLongValue()));
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
