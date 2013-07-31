package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class IntMvField extends IntField implements MultiValueFieldType {
	public IntMvField(String id){
		super(id);
		multiValue = true;
	}
	
	public IntMvField(String id, String data){
		super(id, data);
		multiValue = true;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		int multiValueCount = input.readVInt();
		fieldsData = new ArrayList<Object>(multiValueCount);
		for (int i = 0; i < multiValueCount; i++) {
			((List<Object>) fieldsData).add(Integer.valueOf(input.readInt()));
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
			Integer v = (Integer) list.get(i);
			output.writeInt(v);
		}
	}
	
	@Override
	public FieldDataWriter getDataWriter() throws IOException {
		final List<Integer> list = (List<Integer>) fieldsData;
		return new FieldDataWriter(list) {
			@Override
			protected void writeEachData(Object object, DataOutput output) throws IOException {
				Integer v = (Integer) object;
				output.writeInt(v);
				
			}
		};
	}
}
