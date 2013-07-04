package org.fastcatsearch.ir.field;

import org.fastcatsearch.ir.io.CharVector;

public abstract class StringField extends Field {
	protected final String multiValueDelimiter = "\n";
	
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
		if(data == null){
			return null;
		}
		
		if(size > 0){
			if(data.length() > size){
				data = data.substring(0, size);
			}
		}
		return new CharVector(data);
	}

}
