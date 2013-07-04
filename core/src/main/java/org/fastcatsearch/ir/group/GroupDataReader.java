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

import org.fastcatsearch.ir.settings.FieldSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class GroupDataReader implements Comparable<GroupDataReader>{
	private static Logger logger = LoggerFactory.getLogger(GroupDataReader.class); 
	private GroupEntryList entryList;
	private int pos;
	private int size;
	private FieldSetting.Type type;
	private int totalSearchCount;
	
	public GroupDataReader(GroupEntryList entryList, int totalSearchCount){
		this.entryList = entryList;
		this.size = entryList.size();
		this.pos = -1;
		this.totalSearchCount = totalSearchCount;
	}
	public FieldSetting.Type getType(){
		return type;
	}
	public int totalSearchCount(){
		return totalSearchCount;
	}
	/**
	 * 커서를 앞으로 한칸 전진시킨다. 
	 * @return 엔트리를 읽을수 있는지 여부
	 */
	public boolean next(){
		pos++;
		
		if(pos >= size)
			return false;
		
		return true;
	}
	
	public GroupEntry read(){
		return entryList.getEntry(pos);
	}
	
	public int compareTo(GroupDataReader r) {
		return read().key.compareTo(r.read().key);
	}

}
