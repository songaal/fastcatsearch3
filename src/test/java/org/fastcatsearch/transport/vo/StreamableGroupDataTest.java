package org.fastcatsearch.transport.vo;

import java.io.IOException;
import java.util.Random;

import org.fastcatsearch.common.BytesReference;
import org.fastcatsearch.common.io.BytesStreamInput;
import org.fastcatsearch.common.io.BytesStreamOutput;
import org.fastcatsearch.common.io.StreamInput;
import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupEntryList;
import org.fastcatsearch.ir.group.GroupKey;
import org.fastcatsearch.ir.group.GroupingObject;
import org.junit.Test;

public class StreamableGroupDataTest {

	@Test
	public void test() throws IOException {
		int TEST_GROUP_COUNT = 2;
		int TEST_ENTRY_COUNT = 10;
		
		Random r = new Random(System.currentTimeMillis());
		
		GroupEntryList[] groupEntryListArray = new GroupEntryList[TEST_GROUP_COUNT];
				
		int totalSearchCount = 0;
		for (int i = 0; i < groupEntryListArray.length; i++) {
			
			GroupEntry[] entryList = new GroupEntry[TEST_ENTRY_COUNT];
			int totalcount = 0;
			for (int j = 0; j < entryList.length; j++) {
				GroupKey key = new GroupKey.IntKey(r.nextInt(20));
				int c = r.nextInt(20);
				totalcount += c;
//				entryList[j] = new GroupEntry(key, c, null); //
				GroupingObject obj = new GroupingObject();
				obj.add(r.nextInt(100));
//				obj.add(r.nextLong());
				entryList[j] = new GroupEntry(key, c, obj);
			}
			groupEntryListArray[i] = new GroupEntryList(entryList, entryList.length, totalcount);
			totalSearchCount += totalcount;
			
		}
		GroupData groupData = new GroupData(groupEntryListArray, totalSearchCount);
		
		
		StreamableGroupData data = new StreamableGroupData(groupData);
		
		BytesStreamOutput output = new BytesStreamOutput();
		data.writeTo(output);
		BytesReference ref = output.bytes();
		//----------
		
		StreamInput intput = new BytesStreamInput(ref);
		StreamableGroupData actual = new StreamableGroupData();
		actual.readFrom(intput);
		
		
	}

}
