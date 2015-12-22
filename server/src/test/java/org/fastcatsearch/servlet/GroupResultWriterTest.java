package org.fastcatsearch.servlet;

import org.fastcatsearch.http.writer.GroupResultWriter;
import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupFunctionType;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.value.IntGroupingValue;
import org.fastcatsearch.util.JSONResponseWriter;
import org.fastcatsearch.util.ResponseWriter;
import org.fastcatsearch.util.ResultWriterException;
import org.fastcatsearch.util.XMLResponseWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

public class GroupResultWriterTest {
	
	GroupResults result = null;
	
	@Before
	public void init(){
		
		String[] headerNameList = new String[]{"COUNT"};
		int groupSize = 3;
		int totalSearchCount = 11200;
		result =  new GroupResults(groupSize, totalSearchCount);
		GroupResult groupResult1 = new GroupResult("category", headerNameList, 10000, 3);
		groupResult1.setEntry(0, new GroupEntry("1", new IntGroupingValue(800, GroupFunctionType.COUNT)));
		groupResult1.setEntry(1, new GroupEntry("2", new IntGroupingValue(150, GroupFunctionType.COUNT)));
		groupResult1.setEntry(2, new GroupEntry("3", new IntGroupingValue(50, GroupFunctionType.COUNT)));
		result.add(groupResult1);
		
		GroupResult groupResult2 = new GroupResult("language", headerNameList, 1000, 3);
		groupResult2.setEntry(0, new GroupEntry("Korea", new IntGroupingValue(650, GroupFunctionType.COUNT)));
		groupResult2.setEntry(1, new GroupEntry("Japan", new IntGroupingValue(250, GroupFunctionType.COUNT)));
		groupResult2.setEntry(2, new GroupEntry("America", new IntGroupingValue(100, GroupFunctionType.COUNT)));
		result.add(groupResult2);
		
		GroupResult groupResult3 = new GroupResult("regdate", headerNameList, 200, 3);
		groupResult3.setEntry(0, new GroupEntry(new Date().toString(), new IntGroupingValue(100, GroupFunctionType.COUNT)));
		groupResult3.setEntry(1, new GroupEntry(new Date().toString(), new IntGroupingValue(70, GroupFunctionType.COUNT)));
		groupResult3.setEntry(2, new GroupEntry(new Date().toString(), new IntGroupingValue(30, GroupFunctionType.COUNT)));
		result.add(groupResult3);
	}
	
	@Test
	public void testJson() throws ResultWriterException, IOException {
		StringWriter writer = new StringWriter();
		ResponseWriter resultWriter = new JSONResponseWriter(writer, true, false, false);
		GroupResultWriter groupResultWriter = new GroupResultWriter(resultWriter);
		long searchTime = 1234;
		boolean isSuccess = true;
		groupResultWriter.writeResult(result, searchTime, isSuccess);
		
		
		System.out.println(writer.toString());
	}
	
	@Test
	public void testXML() throws ResultWriterException, IOException {
		StringWriter writer = new StringWriter();
		XMLResponseWriter stringer = new XMLResponseWriter(writer, "fastcatsearch", true, false);
		GroupResultWriter groupResultWriter = new GroupResultWriter(stringer);
		long searchTime = 1234;
		boolean isSuccess = true;
		groupResultWriter.writeResult(result, searchTime, isSuccess);
		
		
		System.out.println(writer.toString());
	}

}
