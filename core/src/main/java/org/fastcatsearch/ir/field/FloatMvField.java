package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.io.Input;
import org.fastcatsearch.ir.io.Output;

public class FloatMvField extends FloatField {
	
	public FloatMvField(String id){
		super(id);
	}
	
	@Override
	public void readFrom(Input input) throws IOException {
		int multiValueCount = input.readVariableByte();
		fieldsData = new ArrayList<Object>(multiValueCount);
		for (int i = 0; i < multiValueCount; i++) {
			((List<Object>) fieldsData).add(Float.intBitsToFloat(input.readInt()));
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
			Float v = (Float) list.get(i);
			output.writeInt(Float.floatToIntBits(v));
		}
	}
	
	@Override
	public FieldDataWriter getDataWriter() throws IOException {
		final List<Float> list = (List<Float>) fieldsData;
		return new FieldDataWriter(list) {
			@Override
			protected void writeEachData(Object object, Output output) throws IOException {
				Float v = (Float) object;
				output.writeInt(Float.floatToIntBits(v));
				
			}
		};
	}

}
