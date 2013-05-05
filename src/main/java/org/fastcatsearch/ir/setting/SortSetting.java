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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "sort")
@XmlType(propOrder = { "sortSize", "byteSize", "name" })
public class SortSetting {
	private static Logger logger = LoggerFactory.getLogger(SortSetting.class);
	
	private String name;
	private FieldSetting field;
	private int sortSize = -1; //사용자가 설정한 정렬길이. 필드길이가 -1일때 즉, 가변길이필드에서는 반드시 정렬길이를 지정해야한다.
	private int byteSize = -1; //실제 바이트사이즈.
	
	public SortSetting() { }
	
	public SortSetting(String name, FieldSetting field){
		this.name = name;
		this.field = field;
	}
	
	public SortSetting(String name, FieldSetting field, int sortSize, int byteSize){
		this.name = name;
		this.field = field;
		this.sortSize = sortSize;
		this.byteSize = byteSize;
	}
	
	public String toString(){
		return "[sort="+name+":"+field+":"+sortSize+":"+byteSize+"]";
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		SortSetting.logger = logger;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlTransient
	public FieldSetting getField() {
		return field;
	}

	public void setField(FieldSetting field) {
		this.field = field;
	}

	@XmlAttribute(name="sort-size")
	public int getSortSize() {
		return sortSize;
	}

	public void setSortSize(int sortSize) {
		this.sortSize = sortSize;
	}

	@XmlAttribute(name="byte-size")
	public int getByteSize() {
		return byteSize;
	}

	public void setByteSize(int byteSize) {
		this.byteSize = byteSize;
	}
}
