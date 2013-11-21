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

package org.fastcatsearch.ir.group;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class GroupFunction {
	protected static Logger logger = LoggerFactory.getLogger(GroupFunction.class);
	
	protected int totalFrequencySum;
	protected int inGroupCount; //임시변수.
	protected int sortOrder;
	protected String fieldId;

	protected String functionName;
	protected GroupingValue[] valueList; //그룹번호별로 데이터를 쌓아놓을 공간확보. 
	
	public GroupFunction(String functionName, int sortOrder, String fieldId){
		this.functionName = functionName.toUpperCase();
		this.sortOrder = sortOrder;
		this.fieldId = fieldId != null ? fieldId.toUpperCase() : null;
	}
	
	public void init(GroupingValue[] valueList){
//		logger.debug("INIT GroupFunction size={}, {}", valueList.length, valueList);
		this.valueList = valueList;
	}
	
	public abstract String getHeaderName();
		
	public GroupingValue[] valueList() {
		return valueList;
	}
	
	public GroupingValue value(int i){
		return valueList[i];
	}
	
	public int valueSize(){
		return valueList.length;
	}
	
	public String name(){
		return functionName;
	}
	
	public abstract void addValue(int groupNo, Number value);

	public abstract void done();
		
}
