package org.fastcatsearch.cluster;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * id 마다 long값을 유지하여 round-robin방식으로 다음 노드를 선택해준다.
 * */
public class NodeLoadBalancer {
	protected static Logger logger = LoggerFactory.getLogger(NodeLoadBalancer.class);
	
	private Map<String, List<Node>> map;
	private Map<String, AtomicLong> sequenceMap;

	public NodeLoadBalancer() {
		map = new ConcurrentHashMap<String, List<Node>>();
		sequenceMap = new ConcurrentHashMap<String, AtomicLong>();
	}

	public void update(String id, List<Node> list) {
		map.put(id, list);
		if (!sequenceMap.containsKey(id)) {
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
		if(l == null || list == null){
			logger.warn("#No node list for {}", id);
			return null;
		}
		Node node = null;
		int tryCount = 0;
		while (tryCount++ < list.size()) {
			long seq = l.getAndIncrement();
			int index = (int) (seq % list.size());
			//logger.debug("getAndIncrement > {}, list.size()={} >>> {}", seq, list.size(), index);
			node = list.get(index);

			if (node.isActive()) {
				return node;
			} else {
				logger.warn("#Node inactive! {}", node);
			}
		}
		logger.warn("#Fail to select node for {}", id);
		return node;

	}

}
