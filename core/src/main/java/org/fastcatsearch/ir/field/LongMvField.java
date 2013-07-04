package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.io.Input;
import org.fastcatsearch.ir.io.Output;

public class LongMvField extends LongField {
	public LongMvField(String id){
		super(id);
		multiValue = true;
	}
	
	@Override
	public void readFrom(Input input) throws IOException {
		int multiValueCount = input.readVariableByte();
		fieldsData = new ArrayList<Object>(multiValueCount);
		for (int i = 0; i < multiValueCount; i++) {
			((List<Object>) fieldsData).add(Long.valueOf(input.readLong()));
		}
	}
	
	@Override
	public void writeTo(Output output) throws IOException {
		List<Object> list = (List<Object>) fieldsData;
		int multiValueCount = list.size();
		output.writeVariableByte(multiValueCount);
		writeFixedDataTo(output);
	}
	
	@Override
	public void writeFixedDataTo(Output output) throws IOException {
		List<Object> list = (List<Object>) fieldsData;
		for (int i = 0; i < list.size(); i++) {
			Long v = (Long) list.get(i);
			output.writeLong(v);
		}
	}
	
	@Override
	public FieldDataWriter getDataWriter() throws IOException {
		final List<Long> list = (List<Long>) fieldsData;
		return new FieldDataWriter(list) {
			@Override
			protected void writeEachData(Object object, Output output) throws IOException {
				Long v = (Long) object;
				output.writeLong(v);
				
			}
		};
	}
}
