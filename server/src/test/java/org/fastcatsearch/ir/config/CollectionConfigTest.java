package org.fastcatsearch.ir.config;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class CollectionConfigTest {

	String configXml = "<collection-config>\n" + 
			"	<name>샘플</name>\n" + 
			"	<index>\n" + 
			"		<pk-term-interval>64</pk-term-interval>\n" + 
			"		<pk-bucket-size>65536</pk-bucket-size>\n" + 
			"		<term-interval>64</term-interval>\n" + 
			"		<bucket-size>65536</bucket-size>\n" + 
			"		<work-memory-size>134217728</work-memory-size>\n" + 
			"		<work-bucket-size>256</work-bucket-size>\n" + 
			"		<read-buffer-size>3072</read-buffer-size>\n" + 
			"		<write-buffer-size>3072</write-buffer-size>\n" + 
			"		<block-size>8</block-size>\n" + 
			"		<compression-type>fast</compression-type>\n" + 
			"	</index>\n" + 
			"	<data-plan>\n" + 
			"		<data-sequence-cycle>2</data-sequence-cycle>\n" + 
			"		<separate-inc-indexing>true</separate-inc-indexing>\n" + 
			"		<segment-document-limit>2000000</segment-document-limit>\n" + 
			"		<segment-revision-backup-size>2</segment-revision-backup-size>\n" + 
			"	</data-plan>\n" + 
			"	<cluster>\n" + 
			"		<index-node>node1</index-node>\n" + 
			"		<data-node>\n" + 
			"			<node>node2</node>\n" + 
			"			<node>node3</node>\n" + 
			"		</data-node>\n" + 
			"		<shard-size>1</shard-size>\n" + 
			"		<replica-size>*</replica-size>\n" + 
			"	</cluster>\n" + 
			"</collection-config>";
	@Test
	public void testRead() throws IOException, JAXBException {
		
		InputStream is = new ByteArrayInputStream(configXml.getBytes());
		CollectionConfig collectionConfig = JAXBConfigs.readConfig(is, CollectionConfig.class);
		assertEquals("샘플", collectionConfig.getName());
		
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
		assertEquals(true, dataPlanConfig.isSeparateIncIndexing());
		
		
		ClusterConfig clusterConfig = collectionConfig.getClusterConfig();
		assertEquals("node1", clusterConfig.getIndexNode());
		List<String> dataNodeList = clusterConfig.getDataNodeList();
		assertEquals("node2", dataNodeList.get(0));
		assertEquals("node3", dataNodeList.get(1));
		
		assertEquals(1, clusterConfig.getShardSize());
		assertEquals("*", clusterConfig.getReplicaSize());
		
	}
	
	
	@Test
	public void testWrite() {
		
		CollectionConfig collectionConfig = new CollectionConfig();
		collectionConfig.setName("샘플");
		collectionConfig.setIndexConfig(new IndexConfig());
		collectionConfig.setDataPlanConfig(new DataPlanConfig());
		collectionConfig.setClusterConfig(new ClusterConfig());
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JAXBConfigs.writeConfig(os, collectionConfig, CollectionConfig.class);
		System.out.println(os.toString());
		
		
	}

}
