package org.fastcatsearch.ir.field;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Field implements Cloneable {
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
	
	public Field(String id, String data) throws FieldDataParseException {
		this.id = id;
		if(data != null){
			this.fieldsData = parseData(data);
		}
	}
	
	public Field(String id, int size){
		this.id = id;
		this.size = size;
	}
	
	public Field(String id, String data, int size) throws FieldDataParseException{
		this.id = id;
		this.size = size;
		if(data != null){
			this.fieldsData = parseData(data);
		}
	}
	
	public String toString(){
		if(fieldsData != null){
			return fieldsData.toString();
		}else{
			return null;
		}
	}
	public void addValues(StringTokenizer tokenizer) throws FieldDataParseException{
		if(!multiValue){
			throw new RuntimeException("multivalue가 아닌 필드에는 값을 추가할 수 없습니다.");
		}
		
		while (tokenizer.hasMoreElements()) {
			addValue(tokenizer.nextToken());
		}
	}
	
	public void addValue(String value) throws FieldDataParseException{
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
	
	protected abstract Object parseData(String data) throws FieldDataParseException;
	
	public abstract void readFrom(DataInput input) throws IOException;
	
	//필드데이터를 기록. string형의 경우 size정보를 앞에 기록한다. document writer에서 사용.
	public abstract void writeTo(DataOutput output) throws IOException;

	//고정길이로 데이터만을 기록. field-index에서 필요. string형의 경우 size정보를 기록하지 않고 데이터만 저장.
	public abstract void writeFixedDataTo(DataOutput output) throws IOException;
	
	//고정길이필드는 고정으로, 가변은 가변으로 데이터만을 기록. string형의 경우 size정보를 기록하지 않고 데이터만 저장.
	public abstract void writeDataTo(DataOutput output) throws IOException;
	
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
	
	public boolean isFixedSize(){
		return size > 0;
	}
	
	@Override
	public Field clone(){
		try {
			return (Field) super.clone();
		} catch (CloneNotSupportedException e) {
			logger.error("clone error", e);
		}
		return null;
	}

}
