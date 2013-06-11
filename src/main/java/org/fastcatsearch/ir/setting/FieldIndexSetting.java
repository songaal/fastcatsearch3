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

@XmlRootElement(name = "field-index")
@XmlType(propOrder = { "ignoreCase", "size", "field", "name", "id" })
public class FieldIndexSetting {
	
	private String id;
	private String name;
	private FieldSetting field;
	private int size;
	private boolean ignoreCase;
	
	public FieldIndexSetting() {}
	
	public FieldIndexSetting(String id, String name, FieldSetting f, int size, boolean ignoreCase) {
		this.id = id;
		this.name = name;
		this.field = f;
		this.size = size;
		this.ignoreCase = ignoreCase;
	}

	@XmlAttribute(required=true)
	public String getId() {
		return id;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	@XmlAttribute(required=true)
	public FieldSetting getField() {
		return field;
	}

	@XmlAttribute
	public int getSize() {
		return size;
	}

	@XmlAttribute
	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setField(FieldSetting field) {
		this.field = field;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public String toString(){
		return "[FieldIndex="+id+":"+name+":"+field+":"+size+":"+ignoreCase+"]";
	}

	
}
