package org.fastcatsearch.ir.config;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.util.JAXBConfigs;
import org.junit.Test;

public class CollectionConfigTest {

	String configXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<collection-config>\n" +
			"	<data-plan>\n" + 
			"		<data-sequence-cycle>2</data-sequence-cycle>\n" + 
			"		<segment-document-limit>2000000</segment-document-limit>\n" + 
			"		<segment-revision-backup-size>2</segment-revision-backup-size>\n" + 
			"	</data-plan>\n" + 
			"	<name>샘플</name>\n" + 
			"   <full-indexing-segment-size>2</full-indexing-segment-size>\n" +
			"   <index-node>node1</index-node>\n" +
			"</collection-config>";
	@Test
	public void testRead() throws IOException, JAXBException {
		
		InputStream is = new ByteArrayInputStream(configXml.getBytes());
		CollectionConfig collectionConfig = JAXBConfigs.readConfig(is, CollectionConfig.class);
		assertEquals("샘플", collectionConfig.getName());
		assertEquals(2, collectionConfig.getFullIndexingSegmentSize().intValue());
		
		DataPlanConfig dataPlanConfig = collectionConfig.getDataPlanConfig();
		assertEquals(2, dataPlanConfig.getDataSequenceCycle());
		assertEquals(2000000, dataPlanConfig.getSegmentDocumentLimit());
		assertEquals(2, dataPlanConfig.getSegmentRevisionBackupSize());
		
		
	}
	
	
	@Test
	public void testWrite() throws JAXBException {
		
		CollectionConfig collectionConfig = new CollectionConfig();
		collectionConfig.setName("샘플");
		collectionConfig.setDataPlanConfig(new DataPlanConfig());
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JAXBConfigs.writeRawConfig(os, collectionConfig, CollectionConfig.class);
		System.out.println(os.toString());
		
		
	}

}
