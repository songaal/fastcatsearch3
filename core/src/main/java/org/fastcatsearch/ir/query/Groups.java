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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.group.GroupDataGenerator;
import org.fastcatsearch.ir.group.GroupsResultGenerator;
import org.fastcatsearch.ir.search.FieldIndexesReader;
import org.fastcatsearch.ir.search.GroupIndexesReader;
import org.fastcatsearch.ir.search.clause.Clause;
import org.fastcatsearch.ir.settings.Schema;



public class Groups {
	
	private List<Group> groupList;
	//그룹 Clause. 그룹핑 연산후에 적용되는 clause. 
	private Clause clause;
	
	public Groups(){
		groupList = new ArrayList<Group>();
	}
	
	public List<Group> getGroupList(){
		return groupList;
	}
	public Group getGroup(int i){
		return groupList.get(i);
	}
	
	public String toString(){
		String str = "";
		for(Group g : groupList){
			str += g.toString()+",";
		}
		return str;
	}
	
	public void add(Group group){
		groupList.add(group);
	}
	
	public int size(){
		return groupList.size();
	}
	public GroupDataGenerator getGroupDataGenerator(Schema schema, GroupIndexesReader groupIndexesReader, FieldIndexesReader fieldIndexesReader) throws IOException{
		return new GroupDataGenerator(groupList, schema, groupIndexesReader, fieldIndexesReader);
	}
	public GroupsResultGenerator getGroupResultsGenerator() {
		return new GroupsResultGenerator(this);
	}
	public Clause getGroupClause(){
		return clause;
	}
	
	public void setGroupFilter(Clause clause){
		this.clause = clause;
	}
	
}
