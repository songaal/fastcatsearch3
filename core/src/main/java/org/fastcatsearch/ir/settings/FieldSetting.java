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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.field.AStringField;
import org.fastcatsearch.ir.field.AStringMvField;
import org.fastcatsearch.ir.field.DatetimeField;
import org.fastcatsearch.ir.field.DoubleField;
import org.fastcatsearch.ir.field.DoubleMvField;
import org.fastcatsearch.ir.field.Field;
import org.fastcatsearch.ir.field.FieldDataParseException;
import org.fastcatsearch.ir.field.FloatField;
import org.fastcatsearch.ir.field.FloatMvField;
import org.fastcatsearch.ir.field.IntField;
import org.fastcatsearch.ir.field.IntMvField;
import org.fastcatsearch.ir.field.LongField;
import org.fastcatsearch.ir.field.LongMvField;
import org.fastcatsearch.ir.field.UStringField;
import org.fastcatsearch.ir.field.UStringMvField;
import org.fastcatsearch.ir.io.IOUtil;
import org.fastcatsearch.util.HTMLTagRemover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "field")
// propOrder는 xml writer의 기록순서인데, attribute의 경우 next attribute를 현재 attribute의 앞에 기록하므로 proporder를 거꾸로 정의해야 올바른 순서로 보여진다.
@XmlType(propOrder = { "multiValueDelimiter", "multiValue", "modify", "removeTag", "compress", "store", "size", "name", "source", "type", "id" })
public class FieldSetting {
	protected static Logger logger = LoggerFactory.getLogger(FieldSetting.class);
	private String id;
	private String name;
	private Type type;
	private int size;
	private String source;
	private boolean store = true;
	private boolean compress;
	private boolean removeTag;
	private boolean modify;
	private boolean multiValue;
	private String multiValueDelimiter;

	public static enum Type {
		UNKNOWN, ASTRING, STRING, INT, LONG, FLOAT, DOUBLE, DATETIME, _SCORE, _HIT, _DOCNO, _BUNDLESIZE, _DISTANCE, _MATCH_ORDER
	}

	// JAXB를 위해서는 default 생성자가 꼭 필요하다.
	public FieldSetting() {
	}

	public FieldSetting(String id, String name, Type type) {
		this.id = id.toUpperCase();
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {
		return "[FieldSetting]" + id + ", type=" + type + ", source=" + source + ", size=" + size + ", mv=" + multiValue;
	}

	@XmlAttribute(required = true)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id.toUpperCase();
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
	@XmlJavaTypeAdapter(OptionalStringAdapter.class)
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source.toUpperCase();
	}
	
	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalIntPositiveAdapter.class)
	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalBooleanTrueAdapter.class)
	//기본적으로 저장 true;
	public Boolean isStore() {
		return store;
	}

	public void setStore(Boolean store) {
		this.store = store;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalBooleanFalseAdapter.class)
	public Boolean isCompress() {
		return compress;
	}

	public void setCompress(Boolean compress) {
		this.compress = compress;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalBooleanFalseAdapter.class)
	public Boolean isRemoveTag() {
		return removeTag;
	}

	public void setRemoveTag(Boolean removeTag) {
		this.removeTag = removeTag;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalBooleanFalseAdapter.class)
	public Boolean isModify() {
		return modify;
	}

	public void setModify(Boolean modify) {
		this.modify = modify;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalBooleanFalseAdapter.class)
	public Boolean isMultiValue() {
		return multiValue;
	}

	public void setMultiValue(Boolean multiValue) {
		this.multiValue = multiValue;
	}

	@XmlAttribute
	public String getMultiValueDelimiter() {
		if ("".equals(multiValueDelimiter)) {
			return null;
		}
		return multiValueDelimiter;
	}

	public void setMultiValueDelimiter(String multiValueDelimiter) {
		this.multiValueDelimiter = multiValueDelimiter;
	}

	//sz가 0보다 크면 sz를 사용하고 아니면 기존 size를 사용한다.
	public int getByteSize(int sz) {
		// string필드의 경우만 sz를 설정할수 있으며, 나머지 필드는 사이즈 셋팅이 불가능하다.
		if (type == Type.STRING) {
			if (sz > 0) {
				return sz * 2;
			}
		} else if (type == Type.ASTRING) {
			if (sz > 0) {
				return sz;
			}
		}
		return getByteSize();

	}

	public int getByteSize() {
		if (type == Type.INT)
			return IOUtil.SIZE_OF_INT;
		else if (type == Type.LONG)
			return IOUtil.SIZE_OF_LONG;
		else if (type == Type.FLOAT)
			return IOUtil.SIZE_OF_INT;
		else if (type == Type.DOUBLE)
			return IOUtil.SIZE_OF_LONG;
		else if (type == Type.DATETIME)
			return IOUtil.SIZE_OF_LONG;
		else if (type == Type.STRING)
			return size * 2;
		else if (type == Type.ASTRING)
			return size;

		return size;
	}

	// 빈 데이터 필드생성. 디스크나 네트워크에서 문서를 읽기전에 빈 객체 필요시 사용.
	public Field createEmptyField() {
		Field field = null;
		try {
			field = createIndexableField(null);
		} catch (FieldDataParseException e) {
			// data가 null일 경우 parse exception은 발생하지 않으므로 무시.
			logger.error("createField 에러.", e);
		}

		return field;
	}

	public Field createIndexableField(Object dataObject) throws FieldDataParseException {
		return createIndexableField(dataObject, null);
	}

	public Field createIndexableField(Object dataObject, String multiValueDelimiter) throws FieldDataParseException {
		String data = null;
		if (dataObject != null) {
			if(dataObject instanceof String){
				data = (String) dataObject;
			}else{
				data = dataObject.toString();
			}
			
			if(removeTag){
				try{
					data = HTMLTagRemover.clean(data);
				}catch(IRException e){
					throw new FieldDataParseException(e);
				}
			}
		}

		Field field = null;
        if (type == FieldSetting.Type.INT || type == FieldSetting.Type.LONG || type == FieldSetting.Type.FLOAT || type == FieldSetting.Type.DOUBLE) {
            if(data != null){
                if(data.length() == 0) {
                    data = null;
                }
            }
        }

		if (type == FieldSetting.Type.INT) {
			if (multiValue) {
				field = new IntMvField(id, data);
			} else {
				field = new IntField(id, data);
			}
		} else if (type == FieldSetting.Type.LONG) {
			if (multiValue) {
				field = new LongMvField(id, data);
			} else {
				field = new LongField(id, data);
			}
		} else if (type == FieldSetting.Type.FLOAT) {
			if (multiValue) {
				field = new FloatMvField(id, data);
			} else {
				field = new FloatField(id, data);
			}
		} else if (type == FieldSetting.Type.DOUBLE) {
			if (multiValue) {
				field = new DoubleMvField(id, data);
			} else {
				field = new DoubleField(id, data);
			}
		} else if (type == FieldSetting.Type.DATETIME) {
			return new DatetimeField(id, data);
		} else if (type == FieldSetting.Type.ASTRING) {
			if (multiValue) {
				field = new AStringMvField(id, data, size);
			} else {
				field = new AStringField(id, data, size);
			}
		} else if (type == FieldSetting.Type.STRING) {
			if (multiValue) {
				field = new UStringMvField(id, data, size);
			} else {
				field = new UStringField(id, data, size);
			}
		}

		if (field == null) {
			throw new RuntimeException("지원하지 않는 필드타입입니다. Type = " + type);
		}

		// 필드스트링을 색인이 용이한 data로 변환한다.
		field.parseIndexable(multiValueDelimiter);

		return field;
	}

	//create variable size data field
	public Field createPatternField(String data) throws FieldDataParseException {
		return createSingleValueField(data, data.length());
	}

	//create fixed size data field
	public Field createPrimaryKeyField(String data) throws FieldDataParseException {
		return createSingleValueField(data, 0);
	}

	public Field createSingleValueField(String data, int length) throws FieldDataParseException {
		if (type == FieldSetting.Type.INT) {
			return new IntField(id, data).parseIndexable();
		} else if (type == FieldSetting.Type.LONG) {
			return new LongField(id, data).parseIndexable();
		} else if (type == FieldSetting.Type.FLOAT) {
			return new FloatField(id, data).parseIndexable();
		} else if (type == FieldSetting.Type.DOUBLE) {
			return new DoubleField(id, data).parseIndexable();
		} else if (type == FieldSetting.Type.DATETIME) {
			return new DatetimeField(id, data).parseIndexable();
		} else if (type == FieldSetting.Type.ASTRING) {
			if (length > 0) {
				return new AStringField(id, data, length).parseIndexable();
			} else {
				return new AStringField(id, data, size).parseIndexable();
			}
		} else if (type == FieldSetting.Type.STRING) {
			if (length > 0) {
				return new UStringField(id, data, length).parseIndexable();
			} else {
				return new UStringField(id, data, size).parseIndexable();
			}
		}

		throw new RuntimeException("지원하지 않는 필드타입입니다. Type = " + type);
	}

	public boolean isVariableField() {
		if (type == FieldSetting.Type.ASTRING || type == FieldSetting.Type.STRING) {
			return size <= 0;
		}

		return false;
	}

	public boolean isNumericField() {
		return type == FieldSetting.Type.INT || type == FieldSetting.Type.LONG || type == FieldSetting.Type.FLOAT || type == FieldSetting.Type.DOUBLE
				|| type == FieldSetting.Type.DATETIME || type == FieldSetting.Type._HIT || type == FieldSetting.Type._SCORE
				|| type == FieldSetting.Type._DOCNO || type == Type._DISTANCE || type == Type._MATCH_ORDER;
	}

}
