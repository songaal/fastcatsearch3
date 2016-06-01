package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.group.*;
import org.fastcatsearch.ir.group.value.*;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamableGroupsData implements Streamable {
	protected static Logger logger = LoggerFactory.getLogger(StreamableGroupsData.class);

	private GroupsData groupsData;

	public StreamableGroupsData() {
	}

	public StreamableGroupsData(GroupsData groupsData) {
		this.groupsData = groupsData;
	}

	public GroupsData groupData() {
		return groupsData;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		int totalSearchCount = input.readVInt();
		int groupSize = input.readVInt();
		List<GroupEntryList> groupEntryListArray = new ArrayList<GroupEntryList>(groupSize);

        GroupFunctionType[] groupFunctionTypeList = GroupFunctionType.values();
		for (int groupNum = 0; groupNum < groupSize; groupNum++) {
			int totalCount = input.readVInt();
			int count = input.readVInt();
			List<GroupEntry> entryList = new ArrayList<GroupEntry>(count);
			for (int j = 0; j < count; j++) {
				String key = input.readString();
				int functionSize = input.readVInt();
				GroupingValue[] valueList = new GroupingValue[functionSize];
				for (int i = 0; i < functionSize; i++) {
                    GroupFunctionType type = groupFunctionTypeList[input.readVInt()];
					Object obj = input.readGenericValue();
					if(obj instanceof Integer){
						valueList[i] = new IntGroupingValue((Integer) obj, type);
					}else if(obj instanceof Long){
						valueList[i] = new LongGroupingValue((Long) obj, type);
					}else if(obj instanceof Float){
						valueList[i] = new FloatGroupingValue((Float) obj, type);
					}else if(obj instanceof Double){
						valueList[i] = new DoubleGroupingValue((Double) obj, type);
					}else if(obj instanceof String){
                        valueList[i] = new StringGroupingValue((String) obj, type);
                    }
				}
				entryList.add(new GroupEntry(key, valueList));
			}

			groupEntryListArray.add(new GroupEntryList(entryList, totalCount));
		}

		groupsData = new GroupsData(groupEntryListArray, totalSearchCount);

	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeVInt(groupsData.totalSearchCount());
		List<GroupEntryList> list = groupsData.list();

		output.writeVInt(list.size());

		for (int groupNum = 0; groupNum < list.size(); groupNum++) {
			GroupEntryList entryList = list.get(groupNum);
			output.writeVInt(entryList.totalCount());
			output.writeVInt(entryList.size());
			for (int j = 0; j < entryList.size(); j++) {
				// 1. write Key
				GroupEntry groupEntry = entryList.getEntry(j);
				// logger.debug("groupEntry >> {}", groupEntry);
				String groupKey = groupEntry.key;
				output.writeString(groupKey);
				
				// 2. group entry list
				output.writeVInt(groupEntry.functionSize());
				for (GroupingValue groupingValue : groupEntry.groupingValues()) {
					if(groupingValue == null) {
                        output.writeVInt(GroupFunctionType.NONE.ordinal());
                        output.writeGenericValue(null);
                    } else {
                        Object result = groupingValue.get();
                        output.writeVInt(groupingValue.getType().ordinal());
                        output.writeGenericValue(result);
                    }
				}
			}

		}
	}

}
