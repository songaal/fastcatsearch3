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

import java.util.Collections;

import org.fastcatsearch.ir.query.Group;
import org.fastcatsearch.ir.query.Groups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 전달받은 frequencyList 를 정렬하고, limit만큼만 잘라준다.
 * 
 * */
public class GroupsResultGenerator {
	protected static Logger logger = LoggerFactory.getLogger(GroupsResultGenerator.class);
	private GroupResults result;
	private Groups groups;
	public GroupsResultGenerator(Groups groups){
		this.groups = groups;
	}
	
	public GroupResults generate(GroupsData groupData) {
		
		//FIXME Assert groupList의 갯수 == groupSettingList 의 갯수.
		// group 쿼리가 문제가 있어서 없어졌을 경우, 어떻게 해야 할까?
		int groupSize = groups.size();
		result = new GroupResults(groupSize, groupData.totalSearchCount());
//		GroupEntryList[] groupDataList = groupData.list();
		
		int i = 0;
		for (GroupEntryList groupEntryList : groupData.list()) {
//			if(groupEntryList.size() == 0){
//				continue;
//			}
			
			Group group = groups.getGroup(i);
			String fieldId = group.fieldId();
			GroupFunction[] groupFunctionList = group.function();
			int limit = group.limit();
			
			//정렬
			groupEntryList.sort(group.sortOrder());
			
			//그룹결과에 표시할 헤더명.
			String[] headerNameList = new String[groupFunctionList.length];
			for (int j = 0; j < headerNameList.length; j++) {
				headerNameList[j] = groupFunctionList[j].getHeaderName();
				logger.debug("group#{} header >> {}", i, headerNameList[j]);
			}
			
			int entryCount = groupEntryList.size();
			limit = (limit > 0 && limit < groupEntryList.size()) ? limit : groupEntryList.size();
			
			//Fill GroupResult
			//GroupResult내부의 array로 GroupEntry의 reference를 복사해준다. 
			GroupResult groupResult = new GroupResult(fieldId, headerNameList, entryCount, limit);
			for (int k = 0; k < limit; k++) {
				GroupEntry e = groupEntryList.getEntry(k);
				logger.debug(">> {}", e);
				//영문의 경우 모두 대문자로 변환되어 있으므로, 첫글자만 대문자 나머지는 소문자로 변환해준다.
				//capitalizeKey는 향후 옵셔널하게 처리한다.
				e.key = e.key.toUpperCase();
				groupResult.setEntry(k, e);
			}
			result.add(groupResult);
			
			i++;
		}
		
		return result;
	}
	
}
