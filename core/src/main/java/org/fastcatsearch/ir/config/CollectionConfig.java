package org.fastcatsearch.ir.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	<full-indexing-alert-timeout>0</full-indexing-alert-timeout>
	<add-indexing-alert-timeout>0</add-indexing-alert-timeout>
</collection-config>
 * */

@XmlRootElement(name = "collection-config")
@XmlType(propOrder = { "name", "indexNode", "searchNodeList", "dataNodeList", "dataPlanConfig", "fullIndexingSegmentSize", "fullIndexingAlertTimeout", "addIndexingAlertTimeout" })
public class CollectionConfig {

	private String name;
	private String indexNode;
	private List<String> searchNodeList;
	private List<String> dataNodeList;
	private DataPlanConfig dataPlanConfig;
	private Integer fullIndexingSegmentSize;
	private Integer fullIndexingAlertTimeout;
	private Integer addIndexingAlertTimeout;

	public CollectionConfig(){
		searchNodeList = new ArrayList<String>();
		dataNodeList = new ArrayList<String>();
	}
	
	public CollectionConfig(String name, String indexNode, List<String> searchNodeList, List<String> dataNodeList, DataPlanConfig dataPlanConfig){
		this.name = name;
		this.indexNode = indexNode;
		this.searchNodeList = searchNodeList;
		this.dataNodeList = dataNodeList;
		this.dataPlanConfig = dataPlanConfig;
		this.fullIndexingSegmentSize = 1;
		this.fullIndexingAlertTimeout = 0;
		this.addIndexingAlertTimeout = 0;
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

	public Set<String> getCollectionNodeIDSet() {
		Set<String> nodeIdSet = new HashSet<String>();
		if(indexNode != null) {
			nodeIdSet.add(indexNode);
		}
		if (searchNodeList != null) {
			for (String nodeStr : searchNodeList) {
				nodeStr = nodeStr.trim();
				if (nodeStr.length() > 0) {
					nodeIdSet.add(nodeStr);
				}
			}
		}
		if (dataNodeList != null) {
			for (String nodeStr : dataNodeList) {
				nodeStr = nodeStr.trim();
				if (nodeStr.length() > 0) {
					nodeIdSet.add(nodeStr);
				}
			}
		}
		return nodeIdSet;
	}
	
	@XmlElement(name="full-indexing-segment-size")
	public Integer getFullIndexingSegmentSize() {
		return fullIndexingSegmentSize != null ? fullIndexingSegmentSize : 1;
	}

	public void setFullIndexingSegmentSize(Integer fullIndexingSegmentSize) {
		this.fullIndexingSegmentSize = fullIndexingSegmentSize;
	}

	@XmlElement(name="full-indexing-alert-timeout")
	public Integer getFullIndexingAlertTimeout() {
		return fullIndexingAlertTimeout != null ? fullIndexingAlertTimeout : 0;
	}

	public void setFullIndexingAlertTimeout(Integer fullIndexingAlertTimeout) {
		this.fullIndexingAlertTimeout = fullIndexingAlertTimeout;
	}

	@XmlElement(name="add-indexing-alert-timeout")
	public Integer getAddIndexingAlertTimeout() {
		return addIndexingAlertTimeout != null ? addIndexingAlertTimeout : 0;
	}

	public void setAddIndexingAlertTimeout(Integer addIndexingAlertTimeout) {
		this.addIndexingAlertTimeout = addIndexingAlertTimeout;
	}
}
