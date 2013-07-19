package org.fastcatsearch.ir.field;

import java.io.IOException;

import org.fastcatsearch.ir.io.DataOutput;

public abstract class NumericField extends Field {
	public NumericField(String id, int size) {
		super(id, size);
	}
	
	public NumericField(String id, String data, int size) throws FieldDataParseException {
		super(id, data, size);
	}

	@Override
	protected Number parseData(String data) throws FieldDataParseException {
		if(data == null){
			return null;
		}
		return parseNumber(data.trim());
	}
	
	@Override
	public void writeTo(DataOutput output) throws IOException {
		writeFixedDataTo(output);
	}
	
	@Override
	public void writeDataTo(DataOutput output) throws IOException {
		writeFixedDataTo(output);
	}

	protected abstract Number parseNumber(String data)  throws FieldDataParseException;
	
}
