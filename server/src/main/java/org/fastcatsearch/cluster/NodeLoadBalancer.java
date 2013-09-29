package org.fastcatsearch.cluster;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class NodeLoadBalancer {

	Map<String, List<Node>> map;
	Map<String, AtomicLong> sequenceMap;
	
	public NodeLoadBalancer(){
		map = new ConcurrentHashMap<String, List<Node>>();
		sequenceMap = new ConcurrentHashMap<String, AtomicLong>();
	}
	
	public void update(String shardId, List<Node> list) {
		map.put(shardId, list);
		if(!sequenceMap.containsKey(shardId)){
			sequenceMap.put(shardId, new AtomicLong());
		}
	}

	public Node getBalancedNode(String shardId) {
		AtomicLong l = sequenceMap.get(shardId);
		List<Node> list = map.get(shardId);
		int index = (int) (l.incrementAndGet() % list.size());
		return list.get(index);
		
	}

}
