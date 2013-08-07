package org.fastcatsearch.ir.search;

import java.util.List;

import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.group.GroupDataMerger;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Groups;

public class GroupResultAggregator {
	Groups groups;
	public GroupResultAggregator(Groups groups){
		this.groups = groups;
	}
	
	public GroupResults aggregate(List<GroupsData> list){
		GroupsData groupData = null;
		//전달받은 data들을 키로 정렬하여 머징한다.
		//TODO 아직까지 키가 변형되는 section은 지원하지 않음. sum가능.
		if(list.size() > 1){
			GroupDataMerger dataMerger = new GroupDataMerger(groups, list.size());
			for (int count = 0; count < list.size(); count++) {
				GroupsData data = list.get(count);
				dataMerger.put(data);
			}
			
			/*
			 * Group Result
			 * */
			groupData = dataMerger.merge();
		}else{
			groupData = list.get(0);
		}
		return groups.getGroupResultsGenerator().generate(groupData);
	}
}
