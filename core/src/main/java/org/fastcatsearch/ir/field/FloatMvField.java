package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.ir.io.Output;

public class FloatMvField extends FloatField implements MultiValueFieldType {
	
	public FloatMvField(String id){
		super(id);
	}
	
	@Override
	public void readFrom(StreamInput input) throws IOException {
		int multiValueCount = input.readVInt();
		fieldsData = new ArrayList<Object>(multiValueCount);
		for (int i = 0; i < multiValueCount; i++) {
			((List<Object>) fieldsData).add(Float.intBitsToFloat(input.readInt()));
		}
		
	}
	
	@Override
	public void writeTo(StreamOutput output) throws IOException {
		List<Object> list = (List<Object>) fieldsData;
		int multiValueCount = list.size();
		output.writeVInt(multiValueCount);
		writeFixedDataTo(output);
	}

	@Override
	public void writeFixedDataTo(StreamOutput output) throws IOException {
		
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
			protected void writeEachData(Object object, StreamOutput output) throws IOException {
				Float v = (Float) object;
				output.writeInt(Float.floatToIntBits(v));
				
			}
		};
	}

}
