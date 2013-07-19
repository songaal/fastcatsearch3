package org.fastcatsearch.ir.field;

import java.io.IOException;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.ir.io.IOUtil;

public class DoubleField extends NumericField {
	
	public DoubleField(String id){
		super(id, IOUtil.SIZE_OF_LONG);
	}
	
	public DoubleField(String id, String data) throws FieldDataParseException{
		super(id, data, IOUtil.SIZE_OF_LONG);
	}
	
	@Override
	protected Double parseNumber(String data){
		return Double.valueOf(data);
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		fieldsData = Double.longBitsToDouble(input.readLong());
	}
	

	@Override
	public void writeFixedDataTo(DataOutput output) throws IOException {
		output.writeLong(Double.doubleToLongBits((Double)fieldsData));
	}

	@Override
	public FieldDataWriter getDataWriter() throws IOException {
		throw new IOException("싱글밸류필드는 writer를 지원하지 않습니다.");
	}
}
