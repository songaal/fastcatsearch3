/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.settings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.fastcatsearch.ir.field.AStringField;
import org.fastcatsearch.ir.field.AStringMvField;
import org.fastcatsearch.ir.field.DatetimeField;
import org.fastcatsearch.ir.field.DoubleField;
import org.fastcatsearch.ir.field.DoubleMvField;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FloatField;
import org.fastcatsearch.ir.field.FloatMvField;
import org.fastcatsearch.ir.field.IntField;
import org.fastcatsearch.ir.field.IntMvField;
import org.fastcatsearch.ir.field.LongField;
import org.fastcatsearch.ir.field.LongMvField;
import org.fastcatsearch.ir.field.UStringField;
import org.fastcatsearch.ir.field.UStringMvField;
import org.fastcatsearch.ir.io.IOUtil;

@XmlRootElement(name = "field")
// propOrder는 xml writer의 기록순서인데, attribute의 경우 next attribute를 현재 attribute의 앞에 기록하므로 proporder를 거꾸로 정의해야 올바른 순서로 보여진다.
@XmlType(propOrder = { "multiValueDelimiter", "multiValue", "modify", "removeTag", "store",
		"size", "name", "type", "id" })
public class FieldSetting {

	private String id;
	private String name;
	private Type type;
	private int size = -1;
	private boolean store = true;
	private boolean removeTag;
	private boolean modify;
	private boolean multiValue;
	private String multiValueDelimiter = "\n";

	public static enum Type {
		UNKNOWN, ASTRING, USTRING, INT, LONG, FLOAT, DOUBLE, DATETIME, BLOB, __SCORE, __HIT, __DOCNO 
	}

	//JAXB를 위해서는 default 생성자가 꼭 필요하다.
	public FieldSetting() {}
	
	public FieldSetting(String id, String name, Type type){
		this.id = id;
		this.name = name;
		this.type = type;
	}
	
	@XmlAttribute(required = true)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(required = true)
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@XmlAttribute
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@XmlAttribute(required = true)
	public boolean isStore() {
		return store;
	}

	public void setStore(boolean store) {
		this.store = store;
	}
	@XmlAttribute
	public boolean isRemoveTag() {
		return removeTag;
	}

	public void setRemoveTag(boolean removeTag) {
		this.removeTag = removeTag;
	}

	@XmlAttribute
	public boolean isModify() {
		return modify;
	}

	public void setModify(boolean modify) {
		this.modify = modify;
	}
	@XmlAttribute
	public boolean isMultiValue() {
		return multiValue;
	}

	public void setMultiValue(boolean multiValue) {
		this.multiValue = multiValue;
	}
	@XmlAttribute
	public String getMultiValueDelimiter() {
		return multiValueDelimiter;
	}

	public void setMultiValueDelimiter(String multiValueDelimiter) {
		this.multiValueDelimiter = multiValueDelimiter;
	}
	
	
	public int getByteSize(){
		if(type == Type.INT)
			return IOUtil.SIZE_OF_INT;
		else if(type == Type.LONG)
			return IOUtil.SIZE_OF_LONG;
		else if(type == Type.FLOAT)
			return IOUtil.SIZE_OF_INT;
		else if(type == Type.DOUBLE)
			return IOUtil.SIZE_OF_LONG;
		else if(type == Type.DATETIME)
			return IOUtil.SIZE_OF_LONG;
		else if(type == Type.USTRING)
			return size * 2;
		else if(type == Type.ASTRING)
			return size;
		
		return size;
	}
	
	public Field createField(){
		if(type == FieldSetting.Type.INT){
			if(multiValue){
				return new IntMvField(id);
			}else{
				return new IntField(id);
			}
		}else if(type == FieldSetting.Type.LONG){
			if(multiValue){
				return new LongMvField(id);
			}else{
				return new LongField(id);
			}
		}else if(type == FieldSetting.Type.FLOAT){
			if(multiValue){
				return new FloatMvField(id);
			}else{
				return new FloatField(id);
			}
		}else if(type == FieldSetting.Type.DOUBLE){
			if(multiValue){
				return new DoubleMvField(id);
			}else{
				return new DoubleField(id);
			}
		}else if(type == FieldSetting.Type.DATETIME){
			return new DatetimeField(id);
		}else if(type == FieldSetting.Type.ASTRING){
			if(multiValue){
				return new AStringMvField(id, size);
			}else{
				return new AStringField(id, size);
			}
		}else if(type == FieldSetting.Type.USTRING){
			if(multiValue){
				return new UStringMvField(id, size);
			}else{
				return new UStringField(id, size);
			}
		}
		
		throw new RuntimeException("지원하지 않는 필드타입입니다. Type = "+ type);
	}
	public Field createPatternField(String data){
		return createSingleValueField(data, data.length());
	}
	public Field createPrimaryKeyField(String data){
		return createSingleValueField(data, 0);
	}
	public Field createSingleValueField(String data, int length){
		if(type == FieldSetting.Type.INT){
			return new IntField(id, data);
		}else if(type == FieldSetting.Type.LONG){
			return new LongField(id, data);
		}else if(type == FieldSetting.Type.FLOAT){
			return new FloatField(id, data);
		}else if(type == FieldSetting.Type.DOUBLE){
			return new DoubleField(id, data);
		}else if(type == FieldSetting.Type.DATETIME){
			return new DatetimeField(id, data);
		}else if(type == FieldSetting.Type.ASTRING){
			if(length > 0){
				return new AStringField(id, data, length);
			}else{
				return new AStringField(id, data, size);
			}
		}else if(type == FieldSetting.Type.USTRING){
			if(length > 0){
				return new UStringField(id, data, length);
			}else{
				return new UStringField(id, data, size);
			}
		}
		
		throw new RuntimeException("지원하지 않는 필드타입입니다. Type = "+ type);
	}

	public boolean isVariableField() {
		if(type == FieldSetting.Type.ASTRING
				|| type == FieldSetting.Type.USTRING){
			return size <= 0;
		}
		
		return false;
	}
	
	public boolean isNumericField() {
		return type == FieldSetting.Type.INT
				|| type == FieldSetting.Type.LONG
				|| type == FieldSetting.Type.FLOAT
				|| type == FieldSetting.Type.DOUBLE
				|| type == FieldSetting.Type.DATETIME
				|| type == FieldSetting.Type.__HIT
				|| type == FieldSetting.Type.__SCORE
				|| type == FieldSetting.Type.__DOCNO;
	}

}
