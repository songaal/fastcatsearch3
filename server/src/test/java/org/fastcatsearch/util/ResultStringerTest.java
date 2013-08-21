package org.fastcatsearch.util;

import static org.junit.Assert.*;

import java.io.StringWriter;

import org.junit.Test;

public class ResultStringerTest {

	@Test
	public void test() throws Exception {
		StringWriter writer = new StringWriter();
		ResultWriter rs = new XMLResultWriter(writer, "fastcatsearch",true);
		
		//rs = new JSONResultStringer();
		
		rs.object()
		.key("status").value(0)
		.key("result").array("item")
			.object()
				.key("no").value(1)
				.key("name").value("name1")
				.key("data").value("data1")
				.endObject()
			.object()
				.key("no").value(2)
				.key("name").value("name2")
				.key("data").value("data2")
				.endObject()
			.object()
				.key("no").value(3)
				.key("name").value("name3")
				.key("data").value("data3")
				.endObject()
		.endArray()
		.key("recursiveList")
			.array("list")
				.array("item1")
					.value("test1-1")
					.value("test1-2")
					.value("test1-3")
				.endArray()
				.array("item2")
					.value("test2-1")
					.value("test2-2")
					.value("test2-3")
				.endArray()
				.array("item3")
					.value("test3-1")
					.value("test3-2")
					.value("test3-3")
				.endArray()
			.endArray()
		.endObject();
		
		rs.done();
		System.out.println(writer.toString());
		
		//fail("Not yet implemented");
	}

	@Test
	public void test2() throws Exception {
		StringWriter writer = new StringWriter();
		ResultWriter rs = new XMLResultWriter(writer, "fastcatsearch",true);
		
		//rs = new JSONResultStringer();
		
		rs.object()
		.key("status").value(0)
		.key("time").value("4ms")
		.key("total_count").value(5)
		.key("field_count").value(3)
		.key("fieldname_list")
		.array("name")
			.value("_no_")
			.value("title")
			.value("body")
			.value("_score_")
		.endArray()
		.key("result")
		.array("item")
			.object()
				.key("no").value(1)
				.key("name").value("name1")
				.key("data").value("data1")
				.endObject()
			.object()
				.key("no").value(2)
				.key("name").value("name2")
				.key("data").value("data2")
				.endObject()
			.object()
				.key("no").value(3)
				.key("name").value("name3")
				.key("data").value("data3")
				.endObject()
		.endArray()
		.endObject();
		
		rs.done();
		
		System.out.println(writer.toString());
		
		//fail("Not yet implemented");
	}

	
	@Test
	public void test3() throws Exception {
		StringWriter writer = new StringWriter();
		ResultWriter rs = new XMLResultWriter(writer, "fastcatsearch",true);
		//ResultWriter rs = new JSONResultWriter(writer,true);
		
		rs.object()
		.key("status").value(0)
		.key("result")
			.object()
				.key("title").value("가나다")
				.key("title2").value("가나다2")
			.endObject()
		.endObject();
		
		rs.done();
		
		System.out.println(writer.toString());
		
		
	}
	
	
}
