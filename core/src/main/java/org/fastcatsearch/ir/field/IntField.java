package org.fastcatsearch.ir.field;

import java.io.IOException;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class IntField extends NumericField {
	
	public IntField(String id){
		super(id);
	}
	
	public IntField(String id, String data){
		super(id, data);
	}
	
	@Override
	protected Integer parseNumber(String data){
		return Integer.valueOf(data);
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		fieldsData = Integer.valueOf(input.readInt());
	}
	
	@Override
	public void writeFixedDataTo(DataOutput output) throws IOException {
		output.writeInt(((Integer)fieldsData).intValue());
	}

	@Override
	public FieldDataWriter getDataWriter() throws IOException {
		throw new IOException("싱글밸류필드는 writer를 지원하지 않습니다.");
	}

}
