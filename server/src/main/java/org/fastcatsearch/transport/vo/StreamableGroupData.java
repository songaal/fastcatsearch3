package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupEntryList;
import org.fastcatsearch.ir.group.GroupingValue;
import org.fastcatsearch.ir.group.value.DoubleGroupingValue;
import org.fastcatsearch.ir.group.value.FloatGroupingValue;
import org.fastcatsearch.ir.group.value.IntGroupingValue;
import org.fastcatsearch.ir.group.value.LongGroupingValue;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamableGroupData implements Streamable {
	protected static Logger logger = LoggerFactory.getLogger(StreamableGroupData.class);

	private GroupData groupData;

	public StreamableGroupData() {
	}

	public StreamableGroupData(GroupData groupData) {
		this.groupData = groupData;
	}

	public GroupData groupData() {
		return groupData;
	}

	@Override
	public void readFrom(DataInput input) throws IOException {
		int totalSearchCount = input.readVInt();
		int groupSize = input.readVInt();
		List<GroupEntryList> groupEntryListArray = new ArrayList<GroupEntryList>(groupSize);

		for (int groupNum = 0; groupNum < groupSize; groupNum++) {
			int totalCount = input.readVInt();
			int count = input.readVInt();
			List<GroupEntry> entryList = new ArrayList<GroupEntry>(count);
			for (int j = 0; j < count; j++) {
				String key = input.readString();
				int functionSize = input.readVInt();
				GroupingValue[] valueList = new GroupingValue[functionSize];
				for (int i = 0; i < functionSize; i++) {
					Object obj = input.readGenericValue();
					if(obj instanceof Integer){
						valueList[i] = new IntGroupingValue((Integer) obj);
					}else if(obj instanceof Long){
						valueList[i] = new LongGroupingValue((Long) obj);
					}else if(obj instanceof Float){
						valueList[i] = new FloatGroupingValue((Float) obj);
					}else if(obj instanceof Double){
						valueList[i] = new DoubleGroupingValue((Double) obj);
					}
				}
				entryList.add(new GroupEntry(key, valueList));
			}

			groupEntryListArray.add(new GroupEntryList(entryList, totalCount));
		}

		groupData = new GroupData(groupEntryListArray, totalSearchCount);

	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeVInt(groupData.totalSearchCount());
		List<GroupEntryList> list = groupData.list();

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
					Object result = groupingValue.get();
					output.writeGenericValue(result);
				}
			}

		}
	}

}
