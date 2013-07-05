package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Field {
	protected static Logger logger = LoggerFactory.getLogger(Field.class);
	protected final String OutputMultiValueDelimiter = "\n";
	
	protected String id;
	protected int size;
	protected boolean store;
	protected boolean removeTag;
	protected boolean multiValue;
	protected Object fieldsData;
	
	public Field(String id){
		this.id = id;
	}
	
	public Field(String id, String data){
		this.id = id;
		this.fieldsData = parseData(data);
	}
	
	public Field(String id, int size){
		this.id = id;
		this.size = size;
	}
	
	public Field(String id, String data, int size){
		this.id = id;
		this.size = size;
		this.fieldsData = parseData(data);
	}
	
	public String toString(){
		if(fieldsData != null){
			return fieldsData.toString();
		}else{
			return null;
		}
	}
	public void addValues(StringTokenizer tokenizer){
		if(!multiValue){
			throw new RuntimeException("multivalue가 아닌 필드에는 값을 추가할 수 없습니다.");
		}
		
		while (tokenizer.hasMoreElements()) {
			addValue(tokenizer.nextToken());
		}
	}
	
	public void addValue(String value){
		if(!multiValue){
			throw new RuntimeException("multivalue가 아닌 필드에는 값을 추가할 수 없습니다.");
		}
		
		if(fieldsData == null){
			fieldsData = new ArrayList<Object>();
		}
		
		if(!(fieldsData instanceof List)){
			Object backUp = fieldsData;
			fieldsData = new ArrayList<Object>();
			((List<Object>) fieldsData).add(backUp);
		}
		
		Object v = parseData(value);
		((List<Object>) fieldsData).add(v);
		
	}
	
	public Object getValue(){
		return fieldsData;
	}
	
	public Iterator<Object> getValueIterator(){
		if(!multiValue){
			throw new RuntimeException("multivalue가 아닌 필드에는 Enumeration을 사용할 수 없습니다.");
		}
		
		if(fieldsData == null){
			return null;
		}
		return ((List<Object>) fieldsData).iterator();
		
	}
	
	public boolean isNull(){
		return fieldsData == null;
	}
	
	protected abstract Object parseData(String data);
	
	public abstract void readFrom(StreamInput input) throws IOException;
	
	//필드데이터를 기록. string형의 경우 size정보가 앞에 붙는다.
	public abstract void writeTo(StreamOutput output) throws IOException;

	//고정길이로 데이터만을 기록. fieldIndex에서 필요. string형의 경우 size정보가 없음.
	public abstract void writeFixedDataTo(StreamOutput output) throws IOException;
	
	//고정길이필드는 고정으로, 가변은 가변으로 데이터만을 기록. string형의 경우 size정보가 없음.
	public abstract void writeDataTo(StreamOutput output) throws IOException;
	
	//멀티밸류의 필드데이터를 하나씩 기록할수 있도록 도와주는 writer. group에서 필드값을 읽을 때 사용됨.
	public abstract FieldDataWriter getDataWriter() throws IOException;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isStore() {
		return store;
	}

	public void setStore(boolean store) {
		this.store = store;
	}

	public boolean isRemoveTag() {
		return removeTag;
	}

	public void setRemoveTag(boolean removeTag) {
		this.removeTag = removeTag;
	}

	public boolean isMultiValue() {
		return multiValue;
	}

	public void setMultiValue(boolean multiValue) {
		this.multiValue = multiValue;
	}

	public Object getFieldsData() {
		return fieldsData;
	}

	public void setFieldsData(Object fieldsData) {
		this.fieldsData = fieldsData;
	}
	
//	public static Field createField(FieldSetting fieldSetting){
//		String fieldId = fieldSetting.getId();
//		if(fieldSetting.getType() == FieldSetting.Type.INT){
//			if(fieldSetting.isMultiValue()){
//				return new IntMvField(fieldId);
//			}else{
//				return new IntField(fieldId);
//			}
//		}else if(fieldSetting.getType() == FieldSetting.Type.LONG){
//			if(fieldSetting.isMultiValue()){
//				return new LongMvField(fieldId);
//			}else{
//				return new LongField(fieldId);
//			}
//		}else if(fieldSetting.getType() == FieldSetting.Type.FLOAT){
//			if(fieldSetting.isMultiValue()){
//				return new FloatMvField(fieldId);
//			}else{
//				return new FloatField(fieldId);
//			}
//		}else if(fieldSetting.getType() == FieldSetting.Type.DOUBLE){
//			if(fieldSetting.isMultiValue()){
//				return new DoubleMvField(fieldId);
//			}else{
//				return new DoubleField(fieldId);
//			}
//		}else if(fieldSetting.getType() == FieldSetting.Type.DATETIME){
//			return new DatetimeField(fieldId);
//		}else if(fieldSetting.getType() == FieldSetting.Type.ASTRING){
//			if(fieldSetting.isMultiValue()){
//				return new AStringMvField(fieldId, fieldSetting.getSize());
//			}else{
//				return new AStringField(fieldId, fieldSetting.getSize());
//			}
//		}else if(fieldSetting.getType() == FieldSetting.Type.USTRING){
//			if(fieldSetting.isMultiValue()){
//				return new UStringMvField(fieldId, fieldSetting.getSize());
//			}else{
//				return new UStringField(fieldId, fieldSetting.getSize());
//			}
//		}
//		
//		throw new RuntimeException("지원하지 않는 필드타입입니다. Type = "+ fieldSetting.getType());
//	}
	
//	public static Field createSingleValueField(FieldSetting fieldSetting, String data){
//		String fieldId = fieldSetting.getId();
//		if(fieldSetting.getType() == FieldSetting.Type.INT){
//			return new IntField(fieldId, data);
//		}else if(fieldSetting.getType() == FieldSetting.Type.LONG){
//			return new LongField(fieldId, data);
//		}else if(fieldSetting.getType() == FieldSetting.Type.FLOAT){
//			return new FloatField(fieldId, data);
//		}else if(fieldSetting.getType() == FieldSetting.Type.DOUBLE){
//			return new DoubleField(fieldId, data);
//		}else if(fieldSetting.getType() == FieldSetting.Type.DATETIME){
//			return new DatetimeField(fieldId, data);
//		}else if(fieldSetting.getType() == FieldSetting.Type.ASTRING){
//			return new AStringField(fieldId, data);
//		}else if(fieldSetting.getType() == FieldSetting.Type.USTRING){
//			return new UStringField(fieldId, data);
//		}
//		
//		throw new RuntimeException("지원하지 않는 필드타입입니다. Type = "+ fieldSetting.getType());
//	}

}
