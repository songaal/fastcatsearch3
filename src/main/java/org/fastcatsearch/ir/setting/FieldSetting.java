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

package org.fastcatsearch.ir.setting;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "field")
// propOrder는 xml writer의 기록순서인데, attribute의 경우 next attribute를 현재 attribute의 앞에 기록하므로 proporder를 거꾸로 정의해야 올바른 순서로 보여진다.
@XmlType(propOrder = { "multiValueMaxCount", "multiValueDelimiter", "multiValue", "modify", "virtual", "removeTag", "store",
		"primary", "size", "name", "type", "id" })
public class FieldSetting {

	private String id;
	private String name;
	private Type type;
	private int size;
	private boolean primary;
	private boolean store;
	private boolean removeTag;
	private boolean virtual;
	private boolean modify;
	private boolean multiValue;
	private char multiValueDelimiter;
	private int multiValueMaxCount;

	public static enum Type {
		UNKNOWN, ACHAR, UCHAR, INT, LONG, FLOAT, DOUBLE, DATETIME, BLOB
	}

	//JAXB를 위해서는 default 생성자가 꼭 필요하다.
	public FieldSetting() {}
	
	public FieldSetting(String id, Type type){
		this.id = id;
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

	@XmlAttribute
	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	@XmlAttribute(required = true)
	public boolean isStore() {
		return store;
	}

	public void setStore(boolean store) {
		this.store = store;
	}

	@XmlAttribute(name = "remove-tag")
	public boolean isRemoveTag() {
		return removeTag;
	}

	public void setRemoveTag(boolean removeTag) {
		this.removeTag = removeTag;
	}

	@XmlAttribute
	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}

	@XmlAttribute
	public boolean isModify() {
		return modify;
	}

	public void setModify(boolean modify) {
		this.modify = modify;
	}

	@XmlAttribute(name = "multi-value")
	public boolean isMultiValue() {
		return multiValue;
	}

	public void setMultiValue(boolean multiValue) {
		this.multiValue = multiValue;
	}

	@XmlAttribute(name = "multi-value-delimiter")
	public char getMultiValueDelimiter() {
		return multiValueDelimiter;
	}

	public void setMultiValueDelimiter(char multiValueDelimiter) {
		this.multiValueDelimiter = multiValueDelimiter;
	}

	@XmlAttribute(name = "multi-value-max-count")
	public int getMultiValueMaxCount() {
		return multiValueMaxCount;
	}

	public void setMultiValueMaxCount(int multiValueMaxCount) {
		this.multiValueMaxCount = multiValueMaxCount;
	}

}
