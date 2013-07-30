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



/**
 * 그루핑 중간결과 객체.
 * 최종 결과를 위해서는 GroupResults로 변경되어야 한다.
 * */
public class GroupResult {

	private String fieldId;
	private String[] headerNameList;
	private int totalCount;
	private int size;
	private GroupEntry[] entryList;
	
	public GroupResult(String fieldId){
		this.fieldId = fieldId;
	}
	
	public GroupResult(String fieldId, String[] headerNameList, int totalCount, int limit){
		this.fieldId = fieldId;
		this.headerNameList = headerNameList;
		this.totalCount = totalCount;
		this.size = limit;
		entryList = new GroupEntry[limit];
	}

	public String fieldId(){
		return fieldId;
	}
	
	public String[] headerNameList(){
		return headerNameList;
	}
	
	public int size(){
		return size;
	}
	
	public int totalCount(){
		return totalCount;
	}
	
	public void setSize(int size){
		this.totalCount = size;
	}
	
	public GroupEntry getEntry(int i){
		return entryList[i];
	}
	
	public void setEntry(int i, GroupEntry entry){
		entryList[i] = entry;
	}
	
}
