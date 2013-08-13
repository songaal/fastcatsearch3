package org.fastcatsearch.ir.config;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
<cluster>
	<index-node>node1</index-node>
	<data-node>
		<node>node2</node>
		<node>node3</node>
	</data-node>
	<shard-size>1</shard-size>
	<replica-size>*</replica-size>
</cluster>
	
 * */
public class ClusterConfig {
	private String indexNode;
	private List<String> dataNodeList;
	private int shardSize;
	private int replicaSize;
	
	@XmlElement(name="index-node")
	public String getIndexNode() {
		return indexNode;
	}
	public void setIndexNode(String indexNode) {
		this.indexNode = indexNode;
	}
	
	@XmlElementWrapper(name="data-node")
	@XmlElement(name="node")
	public List<String> getDataNodeList() {
		return dataNodeList;
	}
	public void setDataNodeList(List<String> dataNodeList) {
		this.dataNodeList = dataNodeList;
	}
	@XmlElement(name="shard-size")
	public int getShardSize() {
		return shardSize;
	}
	public void setShardSize(int shardSize) {
		this.shardSize = shardSize;
	}
	@XmlElement(name="replica-size")
	public int getReplicaSize() {
		return replicaSize;
	}
	public void setReplicaSize(int replicaSize) {
		this.replicaSize = replicaSize;
	}
	
}
