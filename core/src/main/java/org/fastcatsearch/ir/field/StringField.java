package org.fastcatsearch.ir.field;

import java.util.List;

import org.fastcatsearch.ir.io.CharVector;

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
				data = data.substring(0, size);
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

//	@Override
//	public void toUpperCase() {
//		if (fieldsData != null) {
//			if (multiValue) {
//				for (CharVector charVector : (List<CharVector>) fieldsData) {
//					if (charVector != null) {
//						charVector.toUpperCase();
//					}
//				}
//			} else {
//				((CharVector) fieldsData).toUpperCase();
//			}
//		}
//	}
}
