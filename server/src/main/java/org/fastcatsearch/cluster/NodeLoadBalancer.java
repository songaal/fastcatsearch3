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
	//컬렉션별로 seq를 유지하면 동일 노드에 여러 컬렉션을 한번에 검색하게 되므로, 이 방법은 사용하지 않는다.
	private AtomicLong rrSequence; //round robin 시퀀스.
	public NodeLoadBalancer() {
		map = new ConcurrentHashMap<String, List<Node>>();
		rrSequence = new AtomicLong();
	}

	public void update(String id, List<Node> list) {
//		if(!map.containsKey(id)) {
//			map.put(id, list);
//		}
		/*
		* id가 없을때에만 업데이트하도록하면 차후 업데이트가 되지 않는 문제가 발생한다.
		* 그러므로 무조건 put하도록 한다.
		* */
		map.put(id, list);
	}

	/**
	 * round-robin방식으로 노드를 선택한다. node.isActive()를 확인하여 false일 경우 모든 노드를 확인해본다.
	 * active한 노드가 없을 경우 null을 리턴한다.
	 * */
	public Node getBalancedNode(String id) {
		long seq = rrSequence.getAndIncrement();
		List<Node> list = map.get(id);
		Node node = null;
		if(list == null){
			logger.error("cannot find node list for {}", id);
			return null;
		}
		int length = list.size();
		//width만큼 돌면서 노드를 찾는다.
		for (int i = 0; i < length; i++) {
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
