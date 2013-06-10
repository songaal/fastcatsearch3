package org.fastcatsearch.ir.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 각 컬렉션별 셋팅을 가지고 있다.
 * collection/컬렉션명/setting.xml
 * 
 <collection-config>
	<name>샘플</name>
	<index>
		<pk-term-interval>64</pk-term-interval>
		<pk-bucket-size>64K</pk-bucket-size>
		<term-interval>64</term-interval>
		<bucket-size>64K</bucket-size>
		<work-memory-size>128M</work-memory-size>
		<work-bucket-size>256</work-bucket-size>
		<read-buffer-size>3M</read-buffer-size>
		<write-buffer-size>3M</write-buffer-size>
		<block-size>8</block-size>
		<compression-type>fast</compression-type>
	</index>
	<data-plan>
		<data-sequence-cycle>2</data-sequence-cycle>
		<separate-inc-indexing>true</separate-inc-indexing>
		<segment-document-limit>2000000</segment-document-limit>
		<segment-revision-backup-size>2</segment-revision-backup-size>
	</data-plan>
	<cluster>
		<index-node>node1</index-node>
		<data-node>
			<node>node2</node>
			<node>node3</node>
		</data-node>
		<shard-size>1</shard-size>
		<replica-size>*</replica-size>
	</cluster>
</collection-config>
 * */

@XmlRootElement(name = "collection-config")
public class CollectionConfig {

	private String name;
	
	private IndexConfig indexConfig;
	private DataPlanConfig dataPlanConfig;
	private ClusterConfig clusterConfig;
	
	
	@XmlElement(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name = "index")
	public IndexConfig getIndexConfig() {
		return indexConfig;
	}
	
	@XmlElement(name = "data-plan")
	public DataPlanConfig getDataPlanConfig() {
		return dataPlanConfig;
	}
	@XmlElement(name = "cluster")
	public ClusterConfig getClusterConfig() {
		return clusterConfig;
	}
	public void setIndexConfig(IndexConfig indexConfig) {
		this.indexConfig = indexConfig;
	}
	public void setDataPlanConfig(DataPlanConfig dataPlanConfig) {
		this.dataPlanConfig = dataPlanConfig;
	}
	public void setClusterConfig(ClusterConfig clusterConfig) {
		this.clusterConfig = clusterConfig;
	}
	
	
}
