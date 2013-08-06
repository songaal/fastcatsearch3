package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.value.IntGroupingValue;
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
	GroupResults groupResults = null;
	@Before
	public void init(){
		String[] headerNameList = new String[]{"KEY", "COUNT"};
		int groupSize = 3;
		int totalSearchCount = 11200;
		groupResults =  new GroupResults(groupSize, totalSearchCount);
		GroupResult groupResult1 = new GroupResult("", headerNameList, 10000, 3);
		groupResult1.setEntry(0, new GroupEntry("1", new IntGroupingValue(800)));
		groupResult1.setEntry(1, new GroupEntry("2", new IntGroupingValue(150)));
		groupResult1.setEntry(2, new GroupEntry("3", new IntGroupingValue(50)));
		groupResults.add(groupResult1);
		GroupResult groupResult2 = new GroupResult("", headerNameList, 1000, 3);
		groupResult2.setEntry(0, new GroupEntry("Korea", new IntGroupingValue(650)));
		groupResult2.setEntry(1, new GroupEntry("Japan", new IntGroupingValue(250)));
		groupResult2.setEntry(2, new GroupEntry("America", new IntGroupingValue(100)));
		groupResults.add(groupResult2);
		GroupResult groupResult3 = new GroupResult("", headerNameList, 200, 3);
		groupResult3.setEntry(0, new GroupEntry(new Date().toString(), new IntGroupingValue(100)));
		groupResult3.setEntry(1, new GroupEntry(new Date().toString(), new IntGroupingValue(70)));
		groupResult3.setEntry(2, new GroupEntry(new Date().toString(), new IntGroupingValue(30)));
		groupResults.add(groupResult3);
		
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
		result = new Result(data, fieldCount, fieldNameList, count, totalCount, 0);
		result.setGroupResult(groupResults);
		
		
		
	}
	
	@Test
	public void testJson() throws StringifyException, IOException {
		StringWriter writer = new StringWriter();
		SearchResultWriter resultWriter = new SearchResultWriter(writer, true);
		ResultStringer stringer = new JSONResultStringer();
		long searchTime = 1234;
		boolean isSuccess = true;
		resultWriter.writeResult(result, stringer, searchTime, isSuccess);
		
		System.out.println(writer.toString());
	}
	
	@Test
	public void testXML() throws StringifyException, IOException {
		StringWriter writer = new StringWriter();
		SearchResultWriter resultWriter = new SearchResultWriter(writer, true);
		XMLResultStringer stringer = new XMLResultStringer("fastcatsearch", true);
		long searchTime = 1234;
		boolean isSuccess = true;
		resultWriter.writeResult(result, stringer, searchTime, isSuccess);
		
		System.out.println(writer.toString());
	}

}
