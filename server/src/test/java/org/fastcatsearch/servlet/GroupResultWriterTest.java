package org.fastcatsearch.servlet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

import org.fastcatsearch.ir.group.GroupEntry;
import org.fastcatsearch.ir.group.GroupResult;
import org.fastcatsearch.ir.group.GroupResults;
import org.fastcatsearch.ir.group.value.IntGroupingValue;
import org.fastcatsearch.util.JSONResultStringer;
import org.fastcatsearch.util.ResultStringer;
import org.fastcatsearch.util.StringifyException;
import org.fastcatsearch.util.XMLResultStringer;
import org.junit.Before;
import org.junit.Test;

public class GroupResultWriterTest {
	
	GroupResults result = null;
	
	@Before
	public void init(){
		
		String[] headerNameList = new String[]{"KEY", "COUNT"};
		int groupSize = 3;
		int totalSearchCount = 11200;
		result =  new GroupResults(groupSize, totalSearchCount);
		GroupResult groupResult1 = new GroupResult(headerNameList, 10000, 3);
		groupResult1.setEntry(0, new GroupEntry("1", new IntGroupingValue(800)));
		groupResult1.setEntry(1, new GroupEntry("2", new IntGroupingValue(150)));
		groupResult1.setEntry(2, new GroupEntry("3", new IntGroupingValue(50)));
		result.add(groupResult1);
		GroupResult groupResult2 = new GroupResult(headerNameList, 1000, 3);
		groupResult2.setEntry(0, new GroupEntry("Korea", new IntGroupingValue(650)));
		groupResult2.setEntry(1, new GroupEntry("Japan", new IntGroupingValue(250)));
		groupResult2.setEntry(2, new GroupEntry("America", new IntGroupingValue(100)));
		
		
		result.add(groupResult2);
		GroupResult groupResult3 = new GroupResult(headerNameList, 200, 3);
		groupResult3.setEntry(0, new GroupEntry(new Date().toString(), new IntGroupingValue(100)));
		groupResult3.setEntry(1, new GroupEntry(new Date().toString(), new IntGroupingValue(70)));
		groupResult3.setEntry(2, new GroupEntry(new Date().toString(), new IntGroupingValue(30)));
		result.add(groupResult3);
	}
	
	@Test
	public void testJson() throws StringifyException, IOException {
		StringWriter writer = new StringWriter();
		GroupResultWriter groupResultWriter = new GroupResultWriter(writer);
		ResultStringer stringer = new JSONResultStringer();
		long searchTime = 1234;
		boolean isSuccess = true;
		groupResultWriter.writeResult(result, stringer, searchTime, isSuccess);
		
		
		System.out.println(writer.toString());
	}
	
	@Test
	public void testXML() throws StringifyException, IOException {
		StringWriter writer = new StringWriter();
		GroupResultWriter groupResultWriter = new GroupResultWriter(writer);
		XMLResultStringer stringer = new XMLResultStringer("fastcatsearch", true);
		long searchTime = 1234;
		boolean isSuccess = true;
		groupResultWriter.writeResult(result, stringer, searchTime, isSuccess);
		
		
		System.out.println(writer.toString());
	}

}
