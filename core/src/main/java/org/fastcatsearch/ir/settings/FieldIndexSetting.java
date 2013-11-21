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

@XmlRootElement(name = "field-index")
@XmlType(propOrder = { "ignoreCase", "size", "ref", "name", "id" })
public class FieldIndexSetting implements ReferencableFieldSetting {
	
	private String id;
	private String name;
	private String ref;
	private int size;
	private boolean ignoreCase;
	
	public FieldIndexSetting() {}
	
	public FieldIndexSetting(String id, String name, String ref) {
		this.id = id.toUpperCase();
		this.name = name;
		this.ref = ref;
	}
	public FieldIndexSetting(String id, String name, String ref, int size, boolean ignoreCase) {
		this.id = id.toUpperCase();
		this.name = name;
		this.ref = ref.toUpperCase();
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


	@XmlAttribute
	public String getRef() {
		return ref;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalIntPositiveAdapter.class)
	public Integer getSize() {
		return size;
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalBooleanFalseAdapter.class)
	public Boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setId(String id) {
		this.id = id.toUpperCase();
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setRef(String ref) {
		this.ref = ref.toUpperCase();
	}
	
	public void setSize(Integer size) {
		this.size = size;
	}

	public void setIgnoreCase(Boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	public String toString(){
		return "[FieldIndex="+id+":"+name+":"+ref+":"+size+":"+ignoreCase+"]";
	}

	
}
