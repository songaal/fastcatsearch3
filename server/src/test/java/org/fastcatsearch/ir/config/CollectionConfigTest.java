package org.fastcatsearch.ir.config;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.fastcatsearch.ir.config.CollectionConfig.Shard;
import org.fastcatsearch.util.JAXBConfigs;
import org.junit.Test;

public class CollectionConfigTest {

	String configXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
			"<collection-config>\n" + 
			"	<shard-list>\n" + 
			"		<shard id=\"sample1\" />\n" + 
			"		<shard id=\"sample2\" />\n" + 
			"	</shard-list>\n" + 
			"	<data-plan>\n" + 
			"		<data-sequence-cycle>2</data-sequence-cycle>\n" + 
			"		<segment-document-limit>2000000</segment-document-limit>\n" + 
			"		<segment-revision-backup-size>2</segment-revision-backup-size>\n" + 
			"	</data-plan>\n" + 
			"	<index>\n" + 
			"		<term-interval>64</term-interval>\n" + 
			"		<work-bucket-size>256</work-bucket-size>\n" + 
			"		<work-memory-size>134217728</work-memory-size>\n" + 
			"		<pk-bucket-size>65536</pk-bucket-size>\n" + 
			"		<pk-term-interval>64</pk-term-interval>\n" + 
			"	</index>\n" + 
			"	<name>샘플</name>\n" + 
			"</collection-config>";
	@Test
	public void testRead() throws IOException, JAXBException {
		
		InputStream is = new ByteArrayInputStream(configXml.getBytes());
		CollectionConfig collectionConfig = JAXBConfigs.readConfig(is, CollectionConfig.class);
		assertEquals("샘플", collectionConfig.getName());
		
		List<Shard> shardList = collectionConfig.getShardConfigList();
		assertEquals(2, shardList.size());
		
		IndexConfig indexConfig = collectionConfig.getIndexConfig();
		assertEquals(64, indexConfig.getIndexTermInterval());
		assertEquals(256, indexConfig.getIndexWorkBucketSize());
		assertEquals(134217728, indexConfig.getIndexWorkMemorySize());
		assertEquals(65536, indexConfig.getPkBucketSize());
		assertEquals(64, indexConfig.getPkTermInterval());
		
		
		DataPlanConfig dataPlanConfig = collectionConfig.getDataPlanConfig();
		assertEquals(2, dataPlanConfig.getDataSequenceCycle());
		assertEquals(2000000, dataPlanConfig.getSegmentDocumentLimit());
		assertEquals(2, dataPlanConfig.getSegmentRevisionBackupSize());
		
	}
	
	
	@Test
	public void testWrite() throws JAXBException {
		
		CollectionConfig collectionConfig = new CollectionConfig();
		collectionConfig.setName("샘플");
		collectionConfig.setIndexConfig(new IndexConfig());
		collectionConfig.setDataPlanConfig(new DataPlanConfig());
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JAXBConfigs.writeRawConfig(os, collectionConfig, CollectionConfig.class);
		System.out.println(os.toString());
		
		
	}

}
