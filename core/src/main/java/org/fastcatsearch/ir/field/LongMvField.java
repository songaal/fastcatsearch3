package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class LongMvField extends LongField implements MultiValueFieldType {
	public LongMvField(String id){
		super(id);
		multiValue = true;
	}
	
	public LongMvField(String id, String data){
		super(id, data);
		multiValue = true;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		int multiValueCount = input.readVInt();
		fieldsData = new ArrayList<Object>(multiValueCount);
		for (int i = 0; i < multiValueCount; i++) {
			((List<Object>) fieldsData).add(Long.valueOf(input.readLong()));
		}
	}
	
	@Override
	public void writeTo(DataOutput output) throws IOException {
		List<Object> list = (List<Object>) fieldsData;
		int multiValueCount = list.size();
		output.writeVInt(multiValueCount);
		writeFixedDataTo(output, 0);
	}
	
	@Override
	public void writeFixedDataTo(DataOutput output, int indexSize, boolean upperCase) throws IOException {
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
			protected void writeEachData(Object object, DataOutput output) throws IOException {
				Long v = (Long) object;
				output.writeLong(v);
				
			}
		};
	}
}
