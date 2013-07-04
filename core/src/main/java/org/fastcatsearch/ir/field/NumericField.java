package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.fastcatsearch.ir.io.Output;

public abstract class NumericField extends Field {
	public NumericField(String id) {
		super(id);
	}
	
	public NumericField(String id, String data) {
		super(id, data);
	}

	@Override
	protected Number parseData(String data) {
		if(data == null){
			return null;
		}
		return parseNumber(data.trim());
	}
	
	@Override
	public void writeTo(Output output) throws IOException {
		writeFixedDataTo(output);
	}
	
	@Override
	public void writeDataTo(Output output) throws IOException {
		writeFixedDataTo(output);
	}

	protected abstract Number parseNumber(String data);
	
}
