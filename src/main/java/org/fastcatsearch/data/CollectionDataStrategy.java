package org.fastcatsearch.data;

import java.util.List;

import org.fastcatsearch.cluster.Node;

public class CollectionDataStrategy extends DataStrategy {
	String collectionId;

	public CollectionDataStrategy(String collectionId, List<Node> indexNodes, List<Node> dataNodes, int shardCount, int replicaCount) {
		super(indexNodes, dataNodes, shardCount, replicaCount);
		this.collectionId = collectionId;
	}

}
