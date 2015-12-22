package org.fastcatsearch.servlet;

import org.fastcatsearch.http.writer.SearchResultWriter;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupFunctionType;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.value.IntGroupingValue;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Row;
import org.fastcatsearch.util.JSONResponseWriter;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;
import org.fastcatsearch.util.XMLResponseWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

public class SearchResultWriterTest {
	
	Result result = null;
	GroupResults groupResults = null;
	@Before
	public void init(){
		String[] headerNameList = new String[]{"COUNT"};
		int groupSize = 3;
		int totalSearchCount = 11200;
		groupResults =  new GroupResults(groupSize, totalSearchCount);
		GroupResult groupResult1 = new GroupResult("", headerNameList, 10000, 3);
		groupResult1.setEntry(0, new GroupEntry("1", new IntGroupingValue(800, GroupFunctionType.COUNT)));
		groupResult1.setEntry(1, new GroupEntry("2", new IntGroupingValue(150, GroupFunctionType.COUNT)));
		groupResult1.setEntry(2, new GroupEntry("3", new IntGroupingValue(50, GroupFunctionType.COUNT)));
		groupResults.add(groupResult1);
		GroupResult groupResult2 = new GroupResult("", headerNameList, 1000, 3);
		groupResult2.setEntry(0, new GroupEntry("Korea", new IntGroupingValue(650, GroupFunctionType.COUNT)));
		groupResult2.setEntry(1, new GroupEntry("Japan", new IntGroupingValue(250, GroupFunctionType.COUNT)));
		groupResult2.setEntry(2, new GroupEntry("America", new IntGroupingValue(100, GroupFunctionType.COUNT)));
		groupResults.add(groupResult2);
		GroupResult groupResult3 = new GroupResult("", headerNameList, 200, 3);
		groupResult3.setEntry(0, new GroupEntry(new Date().toString(), new IntGroupingValue(100, GroupFunctionType.COUNT)));
		groupResult3.setEntry(1, new GroupEntry(new Date().toString(), new IntGroupingValue(70, GroupFunctionType.COUNT)));
		groupResult3.setEntry(2, new GroupEntry(new Date().toString(), new IntGroupingValue(30, GroupFunctionType.COUNT)));
		groupResults.add(groupResult3);
		
		Row[] data = new Row[2];
		data[0] = makeRow("안녕하세요.", "사랑합니다.");
		data[1] = makeRow("“3D프린터로 아이디어 형상을 현실로 실현하다”", "정성을 다하겠습니다.");
		
		Row[][] bundleData = new Row[2][];
		bundleData[0] = new Row[]{makeRow("안녕하세요111.", "사랑합니다.111"), makeRow("안녕하세요.222", "사랑합니다22222.")};
		bundleData[1] = new Row[]{makeRow("세무회계사무소222.", "정성을 다하겠습니다2222.."), makeRow("세무사무소입니다..", "사랑이 가득한곳.")};
        int[] bundleTotalSizeList = new int[2];
        bundleTotalSizeList[0] = 2;
        bundleTotalSizeList[1] = 2;
		
		
		String[] fieldNameList = new String[]{"title", "body"};
		int count = 2;
		int totalCount = 100;
		result = new Result(data, bundleData, bundleTotalSizeList, null, fieldNameList, count, totalCount, 0, null, null);
		result.setGroupResult(groupResults);
		
	}
	
	private Row makeRow(String... args) {
		Row r = new Row(args.length);
		for(int i= 0; i < args.length ; i++) {
			String a = args[i];
			r.put(i, a.toCharArray());
		}
		return r;
	}
	
	@Test
	public void testJson() throws ResultWriterException, IOException {
		StringWriter writer = new StringWriter();
		ResponseWriter stringer = new JSONResponseWriter(writer);
		SearchResultWriter resultWriter = new SearchResultWriter(stringer);
		long searchTime = 1234;
		boolean isSuccess = true;
		resultWriter.writeResult(result, searchTime, isSuccess);
		
		System.out.println(writer.toString());
	}
	
	@Test
	public void testXML() throws ResultWriterException, IOException {
		StringWriter writer = new StringWriter();
		XMLResponseWriter stringer = new XMLResponseWriter(writer, "fastcatsearch", true, false);
		SearchResultWriter resultWriter = new SearchResultWriter(stringer);
		long searchTime = 1234;
		boolean isSuccess = true;
		resultWriter.writeResult(result, searchTime, isSuccess);
		
		System.out.println(writer.toString());
	}

}
