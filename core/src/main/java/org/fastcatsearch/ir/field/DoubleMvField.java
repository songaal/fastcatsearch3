package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.Input;
import org.fastcatsearch.ir.io.Output;

public class DoubleMvField extends DoubleField {
	
	public DoubleMvField(String id){
		super(id);
	}
	
	@Override
	public void readFrom(Input input) throws IOException {
		int multiValueCount = input.readVariableByte();
		fieldsData = new ArrayList<Object>(multiValueCount);
		for (int i = 0; i < multiValueCount; i++) {
			((List<Object>) fieldsData).add(Double.longBitsToDouble(input.readLong()));
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
			Double v = (Double) list.get(i);
			output.writeLong(Double.doubleToLongBits(v));
		}
	}

	@Override
	public FieldDataWriter getDataWriter() throws IOException {
		final List<Double> list = (List<Double>) fieldsData;
		return new FieldDataWriter(list) {
			@Override
			protected void writeEachData(Object object, Output output) throws IOException {
				Double v = (Double) object;
				output.writeLong(Double.doubleToLongBits(v));
				
			}
		};
	}
}
