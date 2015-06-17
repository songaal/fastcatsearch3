package org.fastcatsearch.util;

import org.junit.Test;

import java.io.StringWriter;

public class ResponseWriterTest {

	@Test
	public void testArrayObject() throws Exception {
		StringWriter writer = new StringWriter();
		ResponseWriter rs = new XMLResponseWriter(writer, "response", true, false);
		
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
	public void testSingle() throws Exception {
		StringWriter writer = new StringWriter();
		ResponseWriter rs = new XMLResponseWriter(writer, "response",true, false);
		
		//rs = new JSONResponseWriter(writer, true);
		
		rs.object()
		.key("collectionId").value("vol1")
		.key("indexNode")
			.object()
			.key("nodeId").value("node2")
			.key("nodeName").value("My node2")
			.endObject()
		.key("dataNode")
			.array()
			.object()
				.key("nodeId").value("node1")
				.key("nodeName").value("My node1")
				.key("segmentSize").value(1)
				.key("revisionUUID").value("adec2d3d03ce48ebb8c897df2f45a233")
				.key("sequence").value(1)
				.key("dataPath").value("data/index1")
				.key("diskSize").value("1GB")
				.key("documentSize").value("3315826")
				.key("createTime").value("2013.12.20 18:06:33")
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
		ResponseWriter rs = new XMLResponseWriter(writer, "fastcatsearch",true, false);
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
