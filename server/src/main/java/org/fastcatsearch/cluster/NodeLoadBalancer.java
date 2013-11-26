package org.fastcatsearch.cluster;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * id 마다 long값을 유지하여 round-robin방식으로 다음 노드를 선택해준다.
 * */
public class NodeLoadBalancer {

	Map<String, List<Node>> map;
	Map<String, AtomicLong> sequenceMap;
	
	public NodeLoadBalancer(){
		map = new ConcurrentHashMap<String, List<Node>>();
		sequenceMap = new ConcurrentHashMap<String, AtomicLong>();
	}
	
	public void update(String id, List<Node> list) {
		map.put(id, list);
		if(!sequenceMap.containsKey(id)){
			sequenceMap.put(id, new AtomicLong());
		}
	}

	/**
	 * round-robin방식으로 노드를 선택한다. node.isActive()를 확인하여 false일 경우 모든 노드를 확인해본다.
	 * active한 노드가 없을 경우 null을 리턴한다. 
	 * */
	public Node getBalancedNode(String id) {
		AtomicLong l = sequenceMap.get(id);
		List<Node> list = map.get(id);
		Node node = null;
		int tryCount = 0;
		do {
			int index = (int) (l.getAndIncrement() % list.size());
			node = list.get(index);
			tryCount++;
		} while(!node.isActive() && tryCount < list.size());
			
		return node;
		
	}

}
