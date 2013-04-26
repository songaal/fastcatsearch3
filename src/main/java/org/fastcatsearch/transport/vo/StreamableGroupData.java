package org.fastcatsearch.transport.vo;

import java.io.IOException;

import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.common.io.StreamOutput;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupEntryList;
import org.fastcatsearch.ir.group.GroupKey;
import org.fastcatsearch.ir.group.GroupingObject;
import org.fastcatsearch.ir.group.GroupKey.DateTimeKey;
import org.fastcatsearch.ir.group.GroupKey.IntKey;
import org.fastcatsearch.ir.group.GroupKey.LongKey;
import org.fastcatsearch.ir.group.GroupKey.StringKey;
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
	public void readFrom(StreamInput input) throws IOException {
		int totalSearchCount = input.readVInt();
		int groupSize = input.readVInt();
		GroupEntryList[] groupEntryListArray = new GroupEntryList[groupSize];

		for (int groupNum = 0; groupNum < groupSize; groupNum++) {
			int totalCount = input.readVInt();
			int count = input.readVInt();
			GroupEntry[] entryList = new GroupEntry[count];
			int totalcount = 0;
			for (int j = 0; j < entryList.length; j++) {
				GroupKey key = null;
				switch (input.readByte()) {
				case 1:
					key = new GroupKey.DateTimeKey(input.readLong());
					break;
				case 2:
					key = new GroupKey.IntKey(input.readVInt());
					break;
				case 3:
					key = new GroupKey.LongKey(input.readVLong());
					break;
				case 4:
					int charSize = input.readVInt();
					char[] chars = new char[charSize];
					for (int i = 0; i < charSize; i++) {
						chars[i] = (char) input.readShort();
					}
					key = new GroupKey.StringKey(chars);
					break;
				}

				int freq = input.readVInt();
				GroupingObject obj = null;
				if (input.readBoolean()) {
					obj = new GroupingObject();
					obj.set(input.readGenericValue());
				}
				entryList[j] = new GroupEntry(key, freq, obj);
				// logger.debug("groupEntry >> {}", entryList[j]);
			}

			groupEntryListArray[groupNum] = new GroupEntryList(entryList, entryList.length, totalcount);
		}

		groupData = new GroupData(groupEntryListArray, totalSearchCount);

	}

	@Override
	public void writeTo(StreamOutput output) throws IOException {
		output.writeVInt(groupData.totalSearchCount());
		GroupEntryList[] list = groupData.list();

		output.writeVInt(list.length);

		for (int groupNum = 0; groupNum < list.length; groupNum++) {
			GroupEntryList entryList = list[groupNum];
			output.writeVInt(entryList.totalFreq());
			output.writeVInt(entryList.size());
			for (int j = 0; j < entryList.size(); j++) {

				// 1. write Key
				GroupEntry groupEntry = entryList.getEntry(j);
				// logger.debug("groupEntry >> {}", groupEntry);
				GroupKey groupKey = groupEntry.key;
				Class<? extends GroupKey> clazz = groupKey.getClass();

				if (clazz == DateTimeKey.class) {
					output.writeByte((byte) 1);
					DateTimeKey key = (DateTimeKey) groupKey;
					output.writeLong(key.key);
				} else if (clazz == IntKey.class) {
					output.writeByte((byte) 2);
					IntKey key = (IntKey) groupKey;
					output.writeVInt(key.key);
				} else if (clazz == LongKey.class) {
					output.writeByte((byte) 3);
					LongKey key = (LongKey) groupKey;
					output.writeVLong(key.key);
				} else if (clazz == StringKey.class) {
					output.writeByte((byte) 4);
					StringKey key = (StringKey) groupKey;
					output.writeVInt(key.length);
					for (int i = 0; i < key.length; i++) {
						output.writeShort((short) key.key[i]);
					}
				}

				// 2. group entry list
				output.writeVInt(groupEntry.count());

				if (groupEntry.groupingObject() != null && groupEntry.groupingObject().getResult() != null) {
					Object result = groupEntry.groupingObject().getResult();
					output.writeBoolean(true);
					output.writeGenericValue(result);
				} else {
					output.writeBoolean(false);
				}

			}

		}
	}

}
