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
import org.fastcatsearch.ir.group.GroupFunctionType;

public class Group {
	
	//Group Sort options
	public static final int SORT_KEY_ASC = 1 << 5;
	public static final int SORT_KEY_DESC = 1 << 6;
	public static final int SORT_VALUE_ASC = 1 << 7;
	public static final int SORT_VALUE_DESC = 1 << 8;
	public static final int SORT_KEY_NUMERIC_ASC = 1 << 9;
	public static final int SORT_KEY_NUMERIC_DESC = 1 << 10;
	
	public static final String DEFAULT_GROUP_FUNCTION_NAME = GroupFunctionType.COUNT.name();
	
	private String groupIndexId;
	private GroupFunction[] functionList;
	private int sortOrder;
	private int limit;
	

	public Group(String groupIndexId, GroupFunction[] functionList, int sortOrder){
		this(groupIndexId, functionList, sortOrder, 0);
	}
	public Group(String groupIndexId, GroupFunction[] functionList, int sortOrder, int limit){
		this.groupIndexId = groupIndexId.toUpperCase();
		this.functionList = functionList;
		this.sortOrder = sortOrder;
		this.limit = limit;
	}
	public String toString(){
		return "[Group]"+groupIndexId+":"+functionList+":"+sortOrder+":"+limit;
	}
	public String groupIndexId(){
		return groupIndexId;
	}
	public GroupFunction[] function(){
		return functionList;
	}
	public int sortOrder(){
		return sortOrder;
	}
	public int limit(){
		return limit;
	}
}
