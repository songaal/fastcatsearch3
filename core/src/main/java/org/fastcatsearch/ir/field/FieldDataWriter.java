package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.io.DataOutput;

public abstract class FieldDataWriter {
	protected int pos;
	protected List<?> list;
	protected boolean upperCase;
	
	public FieldDataWriter(List<?> list){
		this.list = list;
	}
	
	public int count(){
		if(list == null){
			return 0;
		}
		return list.size();
	}
	
	public boolean write(DataOutput output) throws IOException{
		if(pos < list.size()){
			writeEachData(list.get(pos++), output);
			return true;
		}
		return false;
	}
	
	protected abstract void writeEachData(Object object, DataOutput output) throws IOException;

	public void setUpperCase(boolean isIgnoreCase) {
		upperCase = isIgnoreCase;		
	}
		
}
