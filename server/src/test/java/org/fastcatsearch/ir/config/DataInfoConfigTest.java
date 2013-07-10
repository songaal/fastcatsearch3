package org.fastcatsearch.ir.config;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;

import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.util.Formatter;
import org.junit.Test;

public class DataInfoConfigTest {

//	@Test
//	public void testDataInfoRead() throws IOException {
//		InputStream is = new ByteArrayInputStream(collectionsConfigXml.getBytes());
//		CollectionsConfig collectionsConfig = JAXBConfigs.readConfig(is, CollectionsConfig.class);
//		List<Collection> collectionList = collectionsConfig.getCollectionList();
//		Collection collection = collectionList.get(0);
//		assertEquals("sample", collection.getId());
//		assertEquals(false, collection.isActive());
//		
//		collection = collectionList.get(1);
//		assertEquals("sample2", collection.getId());
//		assertEquals(true, collection.isActive());
//	}
	
	@Test
	public void testDataInfoWrite() {
		
		DataInfo dataInfo = new DataInfo();
		dataInfo.setSegmentInfoList(new ArrayList<SegmentInfo>());
		SegmentInfo segmentInfo = new SegmentInfo("0", 0);
		segmentInfo.update("0", 200, 10, Formatter.formatDate(new Date()));
		dataInfo.getSegmentInfoList().add(segmentInfo);
		segmentInfo = new SegmentInfo("1", 1000);
		segmentInfo.update("0", 400, 20, Formatter.formatDate(new Date()));
		dataInfo.getSegmentInfoList().add(segmentInfo);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JAXBConfigs.writeConfig(os, dataInfo, DataInfo.class);
		System.out.println(os.toString());
		
		
	}

	
	@Test
	public void testSegmentInfoWrite() {
		
		SegmentInfo segmentInfo = new SegmentInfo("5", 200);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JAXBConfigs.writeConfig(os, segmentInfo, SegmentInfo.class);
		System.out.println(os.toString());
		
		
	}
}
