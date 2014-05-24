package org.fastcatsearch.cluster;

import java.util.List;
import java.util.Map;
import java.util.Random;
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
	//컬렉션별로 seq를 유지하면 동일 노드에 여러 컬렉션을 한번에 검색하게 되므로, 이 방법은 사용하지 않는다.
//	private Map<String, AtomicLong> sequenceMap;
	private AtomicLong rrSequence; //round robin 시퀀스.
	public NodeLoadBalancer() {
		map = new ConcurrentHashMap<String, List<Node>>();
//		sequenceMap = new ConcurrentHashMap<String, AtomicLong>();
		rrSequence = new AtomicLong();
	}

	public void update(String id, List<Node> list) {
		map.put(id, list);
//		if (!sequenceMap.containsKey(id)) {
//			sequenceMap.put(id, new AtomicLong());
//		}
	}

	/**
	 * round-robin방식으로 노드를 선택한다. node.isActive()를 확인하여 false일 경우 모든 노드를 확인해본다.
	 * active한 노드가 없을 경우 null을 리턴한다.
	 * */
	Random r = new Random(System.currentTimeMillis());
	public Node getBalancedNode(String id) {
//		long seq = rrSequence.getAndIncrement();
		int seq = r.nextInt(1024);
//		AtomicLong l = sequenceMap.get(id);
		List<Node> list = map.get(id);
//		if(l == null || list == null){
//			logger.warn("#No node list for {}", id);
//			return null;
//		}
		Node node = null;
		if(list == null){
			logger.error("cannot find node list for {}", id);
			return null;
		}
		int length = list.size();
		//width만큼 돌면서 노드를 찾는다.
		for (int i = 0; i < length; i++) {
//			long seq = l.getAndIncrement();
			int index = (int) (seq++ % length);
			node = list.get(index);
//			logger.info("{}]getAndIncrement seq[{}], list.size()[{}] index[{}] >> node[{}] isactive[{}]", tryCount - 1, seq, list.size(), index, node, node.isActive());

			if (node.isActive()) {
				return node;
			} else {
//				logger.warn("#Node inactive! {}", node);
			}
		}
		logger.warn("#Fail to select node for {}", id);
		return node;

	}

}
