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

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "index")
@XmlType(propOrder = {"field","handler","queryHandler","name"})
public class IndexSetting {

	private String name;
	//index필드는 여러개의 필드를 받을 수 있다.
	
	private List<RefSetting> field;
	private String handler;
	private String queryHandler;
	
	public IndexSetting() { }
	
	public IndexSetting(String name, List<RefSetting> field){
		this.name = name;
		this.field = field;
	}
	
	public IndexSetting(String name, List<RefSetting> field, String handler, String queryHandler){
		this.name = name;
		this.field = field;
		this.handler = handler;
		this.queryHandler = (queryHandler == null) ? handler : queryHandler;
	}
	
	public String toString(){
		return "[index="+name+":"+field+":"+handler+":"+queryHandler+"]";
	}

	@XmlAttribute(required = true)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public List<RefSetting> getField() {
		return field;
	}

	public void setField(List<RefSetting> field) {
		this.field = field;
	}

	@XmlAttribute
	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	@XmlAttribute
	public String getQueryHandler() {
		return queryHandler;
	}

	public void setQueryHandler(String queryHandler) {
		this.queryHandler = queryHandler;
	}
}
