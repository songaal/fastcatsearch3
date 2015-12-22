package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.fastcatsearch.common.BytesReference;
import org.fastcatsearch.common.io.BytesStreamInput;
import org.fastcatsearch.common.io.BytesStreamOutput;
import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.ir.group.GroupFunctionType;
import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupEntryList;
import org.fastcatsearch.ir.group.value.IntGroupingValue;
import org.junit.Test;

public class StreamableGroupDataTest {

	@Test
	public void test() throws IOException {
		int TEST_GROUP_COUNT = 2;
		int TEST_ENTRY_COUNT = 10;
		
		Random r = new Random(System.currentTimeMillis());
		
		List<GroupEntryList> groupEntryListArray = new ArrayList<GroupEntryList>(TEST_GROUP_COUNT);
				
		int totalSearchCount = 0;
		for (int i = 0; i <TEST_GROUP_COUNT; i++) {
			GroupEntryList groupEntryList = new GroupEntryList();
			groupEntryListArray.add(groupEntryList);
			
			int totalcount = 0;
			for (int j = 0; j < TEST_ENTRY_COUNT; j++) {
				String key = Integer.toString(r.nextInt(20));
				int c = r.nextInt(20);
				totalcount += c;
				IntGroupingValue obj = new IntGroupingValue(r.nextInt(100), GroupFunctionType.COUNT);
				groupEntryList.add(new GroupEntry(key, obj));
			}
			
			totalSearchCount += totalcount;
			
		}
		GroupsData groupData = new GroupsData(groupEntryListArray, totalSearchCount);
		
		
		StreamableGroupsData data = new StreamableGroupsData(groupData);
		
		BytesStreamOutput output = new BytesStreamOutput();
		data.writeTo(output);
		BytesReference ref = output.bytesReference();

		StreamInput intput = new BytesStreamInput(ref);
		StreamableGroupsData actual = new StreamableGroupsData();
		actual.readFrom(intput);
		
		
	}

}
