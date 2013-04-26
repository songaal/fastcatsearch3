package org.fastcatsearch.servlet;

import java.io.StringWriter;

import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupKey.DateTimeKey;
import org.fastcatsearch.ir.group.GroupKey.IntKey;
import org.fastcatsearch.ir.group.GroupKey.StringKey;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.query.Group;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.util.JSONResultStringer;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;
import org.fastcatsearch.util.XMLResultStringer;
import org.junit.Before;
import org.junit.Test;

public class SearchResultWriterTest {
	
	Result result = null;
	
	@Before
	public void init(){
		int groupSize = 3;
		int totalSearchCount = 11200;
		GroupResults groupResult =  new GroupResults(groupSize, totalSearchCount);
		GroupResult groupResult1 = new GroupResult(Group.DEFAULT_AGGREGATION_FUNCTION_NAME, 10000, 3);
		groupResult1.setEntry(0, new GroupEntry(new IntKey(1), 800));
		groupResult1.setEntry(1, new GroupEntry(new IntKey(2), 150));
		groupResult1.setEntry(2, new GroupEntry(new IntKey(3), 50));
		groupResult.add(groupResult1);
		GroupResult groupResult2 = new GroupResult(Group.DEFAULT_AGGREGATION_FUNCTION_NAME, 1000, 3);
		groupResult2.setEntry(0, new GroupEntry(new StringKey("Korea".toCharArray()), 650));
		groupResult2.setEntry(1, new GroupEntry(new StringKey("Japan".toCharArray()), 250));
		groupResult2.setEntry(2, new GroupEntry(new StringKey("America".toCharArray()), 100));
		groupResult.add(groupResult2);
		GroupResult groupResult3 = new GroupResult(Group.DEFAULT_AGGREGATION_FUNCTION_NAME, 200, 3);
		groupResult3.setEntry(0, new GroupEntry(new DateTimeKey(System.currentTimeMillis()), 100));
		groupResult3.setEntry(1, new GroupEntry(new DateTimeKey(System.currentTimeMillis()), 70));
		groupResult3.setEntry(2, new GroupEntry(new DateTimeKey(System.currentTimeMillis()), 30));
		groupResult.add(groupResult3);
		
		Row[] data = new Row[2];
		String field1 = "안녕하세요.";
		String field2 = "사랑합니다.";
		data[0] = new Row(new char[][]{field1.toCharArray(), field2.toCharArray()});
		field1 = "안녕하세요2";
		field2 = "사랑합니다2";
		data[1] = new Row(new char[][]{field1.toCharArray(), field2.toCharArray()});
		
		int fieldCount = 2;
		String[] fieldNameList = new String[]{"title", "body"};
		int count = 2;
		int totalCount = 100;
		Metadata meta = new Metadata(1, count);
		result = new Result(data, fieldCount, fieldNameList, count, totalCount, meta);
		result.setGroupResult(groupResult);
		
		
		
	}
	
	@Test
	public void testJson() throws StringifyException {
		StringWriter writer = new StringWriter();
		SearchResultWriter resultWriter = new SearchResultWriter(writer, true);
		ResultStringer stringer = new JSONResultStringer();
		long searchTime = 1234;
		boolean isSuccess = true;
		resultWriter.writeResult(result, stringer, searchTime, isSuccess);
		
		System.out.println(writer.toString());
	}
	
	@Test
	public void testXML() throws StringifyException {
		StringWriter writer = new StringWriter();
		SearchResultWriter resultWriter = new SearchResultWriter(writer, true);
		XMLResultStringer stringer = new XMLResultStringer("fastcatsearch", true);
		long searchTime = 1234;
		boolean isSuccess = true;
		resultWriter.writeResult(result, stringer, searchTime, isSuccess);
		
		System.out.println(writer.toString());
	}

}
