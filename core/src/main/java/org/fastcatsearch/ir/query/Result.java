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

package org.fastcatsearch.ir.query;

import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.GroupResult;

public class Result {

	private int count;
	private int totalCount;
	private int fieldCount;
	private String[] fieldNameList;
	private Row[] rows;
	private GroupResults groupResults;
	private Metadata meta;
	
	//원문조회기능에서 필요.
	private int docCount;
	private int deletedDocCount;
	private int segmentCount;
	
	public Result(){}
	public Result(Row[] data, int fieldCount, String[] fieldNameList, int count, int totalCount, Metadata meta){
		this(data, null, fieldCount, fieldNameList, count, totalCount, meta);
	}
	public Result(Row[] data, GroupResults groupResults, int fieldCount, String[] fieldNameList, int count, int totalCount, Metadata meta){
		this.rows = data;
		this.groupResults = groupResults;
		this.fieldCount = fieldCount;
		this.fieldNameList = fieldNameList;
		this.count = count;
		this.totalCount = totalCount;
		this.meta = meta;
	}
	
	public int getTotalCount(){
		return totalCount;
	}
	
	public int getCount(){
		return count;
	}
	
	public int getFieldCount(){
		return fieldCount;
	}
	
	public Row[] getData(){
		return rows;
	}
	
	public String[] getFieldNameList(){
		return fieldNameList;
	}
	
	public void setGroupResult(GroupResults groupResult){
		this.groupResults = groupResult;
	}
	
	public GroupResults getGroupResult(){
		return groupResults;
	}
	
	public Metadata getMetadata(){
		return meta;
	}
	
	public String toString(){
		if(groupResults != null){
			return "[Result]count = "+count+", totalCount = "+totalCount+", fieldCount = "+fieldCount+", groupResult.length = "+groupResults.groupSize();
		}else{
			return "[Result]count = "+count+", totalCount = "+totalCount+", fieldCount = "+fieldCount;
		}
	}
	
	public int getDocCount() {
		return docCount;
	}
	public void setDocCount(int docCount) {
		this.docCount = docCount;
	}
	public int getDeletedDocCount() {
		return deletedDocCount;
	}
	public void setDeletedDocCount(int deletedDocCount) {
		this.deletedDocCount = deletedDocCount;
	}
	public int getSegmentCount() {
		return segmentCount;
	}
	public void setSegmentCount(int segmentCount) {
		this.segmentCount = segmentCount;
	}
	
}
