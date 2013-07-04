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

import org.fastcatsearch.ir.group.GroupFunction;
import org.fastcatsearch.ir.group.function.CountGroupFunction;

public class Group {
	
	//Group Sort options
	public static final int SORT_KEY_ASC = 1 << 5;
	public static final int SORT_KEY_DESC = 1 << 6;
	public static final int SORT_VALUE_ASC = 1 << 7;
	public static final int SORT_VALUE_DESC = 1 << 8;
	
	public static final String DEFAULT_GROUP_FUNCTION_NAME = CountGroupFunction.FUNCTION_NAME;
	
	private String fieldname;
//	private String function;
	private GroupFunction[] functionList;
//	private String[] param; //function에 사용될 파라미터 (custom function일 경우 사용자 파라미터)
//	private int sectionCount; //how many sections 
	private int sortOrder;
	private int limit;
	
//	public Group(String fieldname, int function){
//		this(fieldname, function, 5);
//	}
//	public Group(String fieldname, String function, int sectionCount){
//		this(fieldname, function, sectionCount, OPT_FREQ_ASC);
//	}
//	public Group(String fieldname, int function, int sectionCount, int sortOrder){
//		this(fieldname, function, sectionCount, sortOrder, -1);
//	}
//	public Group(String fieldname, int function, int sectionCount, int sortOrder, int limit){
//		
//	}
	public Group(String fieldname, GroupFunction[] functionList, int sortOrder){
		this(fieldname, functionList, sortOrder, 0);
	}
	public Group(String fieldname, GroupFunction[] functionList, int sortOrder, int limit){
		this.fieldname = fieldname;
		this.functionList = functionList;
		this.sortOrder = sortOrder;
		this.limit = limit;
	}
	public String toString(){
		return fieldname+":"+functionList+":"+sortOrder+":"+limit;
//		return fieldname+":"+function+":"+sectionCount+":"+sortOrder+":"+limit;
	}
	public String fieldname(){
		return fieldname;
	}
	public GroupFunction[] function(){
		return functionList;
	}
//	public int sectionCount(){
//		return sectionCount;
//	}
	public int sortOrder(){
		return sortOrder;
	}
	public int limit(){
		return limit;
	}
}
