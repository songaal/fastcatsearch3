package org.fastcatsearch.cluster;

import java.util.List;

public class ClusterStrategy {
	private String collectionId;
	private String indexingNode;
	private List<String> dataNodes;
	private int shardCount;
	private int replicaCount;

	public ClusterStrategy(String collectionId, String indexingNode, List<String> dataNodes, int shardCount, int replicaCount) {
		this.collectionId = collectionId;
		this.indexingNode = indexingNode;
		this.dataNodes = dataNodes;
		this.shardCount = shardCount;
		this.replicaCount = replicaCount;
	}
	
	@Override
	public String toString(){
		return getClass().getSimpleName()+"]"+collectionId+", indexing="+indexingNode+":datas="+dataNodes.size()+", shards="+shardCount+", replicas="+replicaCount;
	}
	public String collectionId(){
		return collectionId;
	}
	
	public String indexingNode(){
		return indexingNode;
	}
	public List<String> dataNodes(){
		return dataNodes;
	}
	public int shardCount(){
		return shardCount;
	}
	public int replicaCount(){
		return replicaCount;
	}
}
