package org.fastcatsearch.ir.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.ir.config.CollectionsConfig.Collection;
import org.fastcatsearch.ir.config.DataInfo.RevisionInfo;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.util.Formatter;
import org.junit.Test;

public class DataInfoConfigTest {

	@Test
	public void testDataInfoRead() throws IOException, JAXBException {
		String dataInfoXml = "<data-info documents=\"7500\" deletes=\"300\">\n" + 
				"	<segment id=\"0\" base=\"0\" revision=\"2\" documents=\"6000\" deletes=\"250\" createTime=\"2013-06-15 15:30:00\" />\n" + 
				"	<segment id=\"1\" base=\"6000\" revision=\"1\" documents=\"1500\" deletes=\"50\" createTime=\"2013-06-15 16:30:00\" />\n" + 
				"</data-info>";
		InputStream is = new ByteArrayInputStream(dataInfoXml.getBytes());
		DataInfo collectionsConfig = JAXBConfigs.readConfig(is, DataInfo.class);
	}
	
	@Test
	public void testDataInfoWrite() {
		
		DataInfo dataInfo = new DataInfo();
		dataInfo.setSegmentInfoList(new ArrayList<SegmentInfo>());
		SegmentInfo segmentInfo = new SegmentInfo("0", 0);
		segmentInfo.updateRevision(new RevisionInfo(0, 200, 10, 0,Formatter.formatDate(new Date())));
		dataInfo.getSegmentInfoList().add(segmentInfo);
		segmentInfo = new SegmentInfo("1", 1000);
		segmentInfo.updateRevision(new RevisionInfo(0, 400, 20, 0, Formatter.formatDate(new Date())));
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
