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
		if(data == null || data.trim().length() == 0){
			return null;
		}

        //숫자형 파싱에 실패하면 null을 보낸다.
        try {
            return parseNumber(data.trim());
        } catch (NumberFormatException e) {
            return null;
        }
	}
	
	@Override
	public void writeTo(DataOutput output) throws IOException {
		writeFixedDataTo(output, 0);
	}
	
	@Override
	public void writeDataTo(DataOutput output, boolean upperCase) throws IOException {
		writeFixedDataTo(output, 0, upperCase);
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
