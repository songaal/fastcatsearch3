package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.List;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataOutput;

public abstract class StringField extends Field {

	public StringField(String id) {
		super(id);
	}

	public StringField(String id, String data) {
		super(id, data);
	}

	public StringField(String id, int size) {
		super(id, size);
	}

	public StringField(String id, String data, int size) {
		super(id, data, size);
	}

	@Override
	protected Object parseData(String data) {
		if (data == null) {
			return null;
		}

		if (size > 0) {
			if (data.length() > size) {
//				logger.debug("Parse data1 {} >> {}", id, data);
				data = data.substring(0, size);
//				logger.debug("Parse data2 {} >> {}, {}", id, size, data);
			}
		}
		return new CharVector(data);
	}

	@Override
	public String getDataString() {
		if (multiValue) {
			StringBuilder sb = new StringBuilder();
			final List<CharVector> list = (List<CharVector>) fieldsData;
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					CharVector charVector = (CharVector) list.get(i);
					sb.append(charVector.toString());
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
