package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.io.CharVector;
import org.fastcatsearch.ir.io.Input;
import org.fastcatsearch.ir.io.Output;

public class UStringMvField extends UStringField {

	public UStringMvField(String id) {
		super(id);
		multiValue = true;
	}
	
	public UStringMvField(String id, int size) {
		super(id);
		this.size = size;
		multiValue = true;
		
	}
	
	@Override
	public void readFrom(Input input) throws IOException {
		int multiValueCount = input.readVariableByte();
		fieldsData = new ArrayList<CharVector>(multiValueCount);
		for (int i = 0; i < multiValueCount; i++) {
			char[] chars = input.readUString();
			((List<CharVector>) fieldsData).add(new CharVector(chars, 0, chars.length));
		}
	}

	@Override
	public void writeTo(Output output) throws IOException {
		List<CharVector> list = (List<CharVector>) fieldsData;
		int multiValueCount = list.size();
		output.writeVariableByte(multiValueCount);
		//고정길이인지 가변길이인지 
		if(size > 0){
			//고정길이
			for (int i = 0; i < list.size(); i++) {
				CharVector charVector = (CharVector) list.get(i);
				output.writeVariableByte(size);
				output.writeUChars(charVector.array, charVector.start, size);
				if(charVector.length < size){
					for (int j = 0; j < size - charVector.length; j++) {
						output.writeUChar(0);
					}
				}
			}
		}else{
			//가변길이
			for (int i = 0; i < multiValueCount; i++) {
				CharVector charVector = (CharVector) list.get(i);
				output.writeUString(charVector.array, charVector.start, charVector.length);
			}
		}

	}
	
	@Override
	public void writeFixedDataTo(Output output) throws IOException {
		//multi value필드는 데이터가 없으면 기록하지 않는다.
		if(fieldsData == null){
			return;
		}
		
		if(size > 0){
			List<CharVector> list = (List<CharVector>) fieldsData;
			for (int i = 0; i < list.size(); i++) {
				CharVector charVector = (CharVector) list.get(i);
				output.writeUChars(charVector.array, charVector.start, size);
				if(charVector.length < size){
					for (int j = 0; j < size - charVector.length; j++) {
						output.writeUChar(0);
					}
				}
			}
		}else{
			throw new IOException("가변길이필드는 지원하지 않습니다.");
		}
	}

	@Override
	public final void writeDataTo(Output output) throws IOException {
		if(size > 0){
			writeFixedDataTo(output);
		}else{
			List<CharVector> list = (List<CharVector>) fieldsData;
			for (int i = 0; i < list.size(); i++) {
				CharVector charVector = (CharVector) list.get(i);
				output.writeUChars(charVector.array, charVector.start, charVector.length);
			}
		}
		
	}
	
	
	@Override
	public FieldDataWriter getDataWriter() throws IOException {
		final List<CharVector> list = (List<CharVector>) fieldsData;
		return new FieldDataWriter(list) {
			@Override
			protected void writeEachData(Object object, Output output) throws IOException {
				CharVector charVector = (CharVector) object;
				if(size > 0){
					
					output.writeUChars(charVector.array, charVector.start, size);
					if(charVector.length < size){
						for (int j = 0; j < size - charVector.length; j++) {
							output.writeAChar(0);
						}
					}
				}else{
					//가변길이.
					output.writeAChars(charVector.array, charVector.start, charVector.length);
				}
			}
		};
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		final List<CharVector> list = (List<CharVector>) fieldsData;
		for (int i = 0; i < list.size(); i++) {
			CharVector charVector = (CharVector) list.get(i);
			sb.append(charVector.toString());
			if(i < list.size() - 1){
				sb.append(multiValueDelimiter);
			}
		}
		
		return sb.toString();
	}
}
