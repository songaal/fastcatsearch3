package org.fastcatsearch.data;

import java.util.List;

import org.fastcatsearch.cluster.Node;

public class DataStrategy {
	List<Node> indexNodes;
	List<Node> dataNodes;
	int shardCount;
	int replicaCount;

	public DataStrategy(List<Node> indexNodes, List<Node> dataNodes, int shardCount, int replicaCount) {
		this.indexNodes = indexNodes;
		this.dataNodes = dataNodes;
		this.shardCount = shardCount;
		this.replicaCount = replicaCount;
	}
	
	public List<Node> indexNodes(){
		return indexNodes;
	}
	public List<Node> dataNodes(){
		return dataNodes;
	}
	public int shardCount(){
		return shardCount;
	}
	public int replicaCount(){
		return replicaCount;
	}
}
