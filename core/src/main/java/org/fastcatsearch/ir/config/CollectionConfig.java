package org.fastcatsearch.ir.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 각 컬렉션별 셋팅을 가지고 있다.
 * collections/컬렉션명/config.xml
 * 
 <collection-config id="sample">
	<name>샘플</name>
	<index-node>node1</index-node>
	<search-node-list>
    	<node id="node1"/>
    </search-node-list>
	<data-node-list>
    	<node id="node1"/>
    	<node id="node2"/>
    </data-node-list>
	<data-plan>
		<data-sequence-cycle>2</data-sequence-cycle>
		<segment-document-limit>2000000</segment-document-limit>
		<segment-revision-backup-size>2</segment-revision-backup-size>
	</data-plan>
</collection-config>
 * */

@XmlRootElement(name = "collection-config")
@XmlType(propOrder = { "name", "indexNode", "searchNodeList", "dataNodeList", "dataPlanConfig" })
public class CollectionConfig {

	private String name;
	private String indexNode;
	private List<String> searchNodeList;
	private List<String> dataNodeList;
	private DataPlanConfig dataPlanConfig;
	
	public CollectionConfig(){
	}
	
	public CollectionConfig(String name, String indexNode, List<String> dataNodeList, DataPlanConfig dataPlanConfig){
		this.name = name;
		this.indexNode = indexNode;
		this.dataNodeList = dataNodeList;
		this.dataPlanConfig = dataPlanConfig;
	}
	

	@XmlElement(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name="index-node", required=true)
	public String getIndexNode() {
		return indexNode;
	}
	public void setIndexNode(String indexNode) {
		this.indexNode = indexNode;
	}
	
	@XmlElementWrapper(name="search-node-list", required=true)
	@XmlElement(name="node")
	public List<String> getSearchNodeList() {
		return searchNodeList;
	}
	
	public void setSearchNodeList(List<String> searchNodeList) {
		this.searchNodeList = searchNodeList;
	}
	
	@XmlElementWrapper(name="data-node-list", required=true)
	@XmlElement(name="node")
	public List<String> getDataNodeList() {
		return dataNodeList;
	}

	public void setDataNodeList(List<String> dataNodeList) {
		this.dataNodeList = dataNodeList;
	}
	
	@XmlElement(name = "data-plan")
	public DataPlanConfig getDataPlanConfig() {
		return dataPlanConfig;
	}
	
	public void setDataPlanConfig(DataPlanConfig dataPlanConfig) {
		this.dataPlanConfig = dataPlanConfig;
	}

}
