package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class DoubleMvField extends DoubleField implements MultiValueFieldType {
	
	public DoubleMvField(String id){
		super(id);
		multiValue = true;
		fieldsData = new ArrayList<Object>();
	}
	
	public DoubleMvField(String id, String data){
		super(id, data);
		multiValue = true;
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		int multiValueCount = input.readVInt();
		fieldsData = new ArrayList<Object>(multiValueCount);
		for (int i = 0; i < multiValueCount; i++) {
			((List<Object>) fieldsData).add(Double.longBitsToDouble(input.readLong()));
		}
		
	}
	
	@Override
	public void writeTo(DataOutput output) throws IOException {
		List<Object> list = (List<Object>) fieldsData;
		int multiValueCount = list.size();
		output.writeVInt(multiValueCount);
		writeFixedDataTo(output, 0, false);
	}

	@Override
	public void writeFixedDataTo(DataOutput output, int indexSize, boolean upperCase) throws IOException {
		
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
			protected void writeEachData(Object object, DataOutput output) throws IOException {
				Double v = (Double) object;
				output.writeLong(Double.doubleToLongBits(v));
				
			}
		};
	}
}
