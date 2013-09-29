package org.fastcatsearch.cluster;

import java.util.List;

public interface NodeLoadBalancable {
	public void updateLoadBalance(String shardId, List<String> dataNodeIdList);
	
	public Node getBalancedNode(String shardId);
	
}
