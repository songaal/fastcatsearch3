package org.fastcatsearch.ir.field;

import java.io.IOException;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;

public class UStringField extends StringField {

	public UStringField(String id) {
		super(id);
	}
	public UStringField(String id, String data) {
		super(id, data);
	}
	
	public UStringField(String id,int size) {
		super(id, size);
	}
	
	public UStringField(String id, String data, int size) {
		super(id, data, size);
	}
	
	@Override
	public void readFrom(DataInput input) throws IOException {
		char[] chars = input.readUString();
		fieldsData = new CharVector(chars, 0, chars.length);
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		CharVector charVector = ((CharVector) fieldsData);
		output.writeUString(charVector.array, charVector.start, charVector.length);
	}

	@Override
	public void writeFixedDataTo(DataOutput output) throws IOException {
		
		if(size > 0){
			//고정길이 single value필드는 데이터가 없으면 고정길이만큼 0으로 기록한다.
			if(fieldsData == null){
				for (int j = 0; j < size; j++) {
					output.writeUChar(0);
				}
				return;
			}
			
			CharVector charVector = (CharVector) fieldsData;
			output.writeUChars(charVector.array, charVector.start, size);
			if(charVector.length < size){
				for (int j = 0; j < size - charVector.length; j++) {
					output.writeUChar(0);
				}
			}
		}else{
			throw new IOException("가변길이필드는 지원하지 않습니다.");
		}
	}
	
	@Override
	public void writeDataTo(DataOutput output) throws IOException {
		CharVector charVector = ((CharVector) fieldsData);
		if(size > 0){
			writeFixedDataTo(output);
		}else{
			output.writeUChars(charVector.array, charVector.start, charVector.length);
		}
		
	}
	
	@Override
	public FieldDataWriter getDataWriter() throws IOException {
		throw new IOException("싱글밸류필드는 writer를 지원하지 않습니다.");
	}
}
