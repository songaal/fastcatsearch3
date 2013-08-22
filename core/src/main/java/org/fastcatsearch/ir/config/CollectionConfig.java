package org.fastcatsearch.ir.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 각 컬렉션별 셋팅을 가지고 있다.
 * collections/컬렉션명/config.xml
 * 
 <collection-config>
	<name>샘플</name>
	<shard-list>
		<shard id="vol1" name="" />
		<shard id="vol2" name="" />
	</shard-list>
	<index>
		<pk-term-interval>64</pk-term-interval>
		<pk-bucket-size>64K</pk-bucket-size>
		<term-interval>64</term-interval>
		<bucket-size>64K</bucket-size>
		<work-memory-size>128M</work-memory-size>
		<work-bucket-size>256</work-bucket-size>
	</index>
	<data-plan>
		<data-sequence-cycle>2</data-sequence-cycle>
		<separate-inc-indexing>true</separate-inc-indexing>
		<segment-document-limit>2000000</segment-document-limit>
		<segment-revision-backup-size>2</segment-revision-backup-size>
	</data-plan>
</collection-config>
 * */

@XmlRootElement(name = "collection-config")
@XmlType(propOrder = { "name", "shardConfigList", "indexConfig", "dataPlanConfig" })
public class CollectionConfig {

	private String name;
	
	private IndexConfig indexConfig;
	private DataPlanConfig dataPlanConfig;
	private List<Shard> shardConfigList;
	
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
	
	public void setIndexConfig(IndexConfig indexConfig) {
		this.indexConfig = indexConfig;
	}
	
	@XmlElement(name = "data-plan")
	public DataPlanConfig getDataPlanConfig() {
		return dataPlanConfig;
	}
	
	public void setDataPlanConfig(DataPlanConfig dataPlanConfig) {
		this.dataPlanConfig = dataPlanConfig;
	}
	
	@XmlElement
	@XmlElementWrapper(name="shard-list")
	public List<Shard> getShardConfigList() {
		return shardConfigList;
	}
	public void setShardConfigList(List<Shard> shardConfigList) {
		this.shardConfigList = shardConfigList;
	}

	public static class Shard {
		String id;
		String name;
		
		@XmlAttribute
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		@XmlAttribute
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		
	}
}
