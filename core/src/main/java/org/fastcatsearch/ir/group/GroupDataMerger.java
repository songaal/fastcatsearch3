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

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.io.FixedMinHeap;
import org.fastcatsearch.ir.query.Group;
import org.fastcatsearch.ir.query.Groups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupDataMerger {
	private static Logger logger = LoggerFactory.getLogger(GroupDataMerger.class);
	private FixedMinHeap<GroupDataReader>[] heapList;
	private GroupFunction[][] groupFunctionList;
	private int totalSearchCount;
	private GroupEntry prevEntry;
	private int groupSize;
	
	public GroupDataMerger(Groups groups, int segmentSize) {
		groupSize = groups.size();
		heapList = new FixedMinHeap[groupSize];
		groupFunctionList = new GroupFunction[groupSize][];
		for (int i = 0; i < groupSize; i++) {
			Group group = groups.getGroupList().get(i);
			heapList[i] = new FixedMinHeap<GroupDataReader>(segmentSize);
			groupFunctionList[i] = group.function();
		}
	}

	public void put(int groupNum, GroupDataReader reader) {
		// push all reader to each heap
		// Push if there is a next entry
		// Calling 'next' makes reader start.
		if (reader.next()) {
			totalSearchCount += reader.totalSearchCount();
			heapList[groupNum].push(reader);
		}
	}

	public void put(GroupsData groupData) {
		totalSearchCount += groupData.totalSearchCount();
		for (int groupNum = 0; groupNum < groupData.groupSize(); groupNum++) {
			GroupDataReader reader = groupData.getGroupDataReader(groupNum);
			if (reader.next()) {
				heapList[groupNum].push(reader);
			}
		}

	}

	public GroupsData merge() {

		List<GroupEntryList> list = new ArrayList<GroupEntryList>(heapList.length);
		for (int groupNum = 0; groupNum < groupSize; groupNum++) {
			prevEntry = null; //초기화.
			GroupEntryList groupEntryList = new GroupEntryList();
			// loop until heapList has no readers
			while (heapList[groupNum].size() > 0) {
				GroupDataReader r = heapList[groupNum].peek();
				GroupEntry entry = r.read();

				if (prevEntry == null) {
					prevEntry = entry;
				} else if (prevEntry.key.equals(entry.key)) {
					// 같은 그룹.
					prevEntry.merge(entry);
				} else {
					// 다른 그룹출현.
//					logger.debug("groupEntryList.add2 {}", prevEntry);
					groupEntryList.add(prevEntry);
					prevEntry = entry;
				}

				if (!r.next()) {
					// if GroupEntry is read all, then remove it.
					heapList[groupNum].pop();
				}

				heapList[groupNum].heapify();
			}
			if(prevEntry != null){
//				logger.debug("groupEntryList.add3 {}", prevEntry);
				groupEntryList.add(prevEntry);
			}

			list.add(groupEntryList);
		}
		return new GroupsData(list, totalSearchCount);
	}

}
