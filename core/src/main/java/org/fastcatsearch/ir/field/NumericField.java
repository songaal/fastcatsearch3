package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataOutput;

public abstract class NumericField extends Field {
	public NumericField(String id, int size) {
		super(id, size);
	}
	
	public NumericField(String id, String data, int size) {
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
	
	@Override
	public String getDataString() {
		if (isMultiValue()) {
			StringBuilder sb = new StringBuilder();
			final List<Object> list = (List<Object>) fieldsData;
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					sb.append(list.get(i).toString());
					if (i < list.size() - 1) {
						sb.append(DEFAULT_MULTI_VALUE_DELIMITER);
					}
				}
			} else {
				return null;
			}

			return sb.toString();
		} else {
			return super.getDataString();
		}
	}
}
