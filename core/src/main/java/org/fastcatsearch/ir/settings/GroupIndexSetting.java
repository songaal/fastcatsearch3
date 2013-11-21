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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(propOrder = { "ignoreCase", "ref", "name", "id"})
public class GroupIndexSetting implements ReferencableFieldSetting {
	
	private String id;
	private String name;
	private String ref;
	private boolean ignoreCase;
	
	public GroupIndexSetting() {}
	
	public GroupIndexSetting(String id, String name, String ref){
		this.id = id.toUpperCase();
		this.name = name;
		this.ref = ref.toUpperCase();
	}
	
	public String toString(){
		return "[group="+id+":"+name+":"+ref+":"+ignoreCase+"]";
	}

	@XmlAttribute(name="id", required=true)
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

	@XmlAttribute
	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref.toUpperCase();
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(OptionalBooleanFalseAdapter.class)
	public Boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(Boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

	

}
