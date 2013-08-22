package org.fastcatsearch.cluster;

import java.util.List;

import org.fastcatsearch.ir.config.ClusterConfig.ShardClusterConfig;

public class ClusterStrategy {
	private String shardId;
	private List<ShardClusterConfig> shardList;

	
	public ClusterStrategy(String shardId, List<ShardClusterConfig> shardList) {
		this.shardId = shardId;
		this.shardList = shardList;
	}

	@Override
	public String toString(){
		return getClass().getSimpleName()+"]"+shardId;
	}
	public String shardId(){
		return shardId;
	}
	
	public String indexingNode(){
		return null;//indexingNode;
	}
	public List<String> dataNodes(){
		return null;//dataNodes;
	}
	public int shardCount(){
		return 0;//shardCount;
	}
	public int replicaCount(){
		return 0;//replicaCount;
	}
}
