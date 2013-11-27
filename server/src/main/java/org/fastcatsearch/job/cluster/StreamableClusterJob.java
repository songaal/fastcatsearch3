package org.fastcatsearch.job.cluster;

import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.Job;

public abstract class StreamableClusterJob extends Job implements Streamable {

	private static final long serialVersionUID = 2080251241965769963L;
	
	protected boolean sendJobToNodeList(Job job, NodeService nodeService, List<Node> nodeList, boolean includeMyNode) throws FastcatSearchException{
		List<ResultFuture> resultFutureList = new ArrayList<ResultFuture>(nodeList.size());
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			if(!includeMyNode && nodeService.isMyNode(node)){
				//자신에게는 실행요청하지 않음.
				continue;
			}
			ResultFuture resultFuture = nodeService.sendRequest(node, job);
			resultFutureList.add(resultFuture);
		}
		for (int i = 0; i < resultFutureList.size(); i++) {
			Node node = nodeList.get(i);
			ResultFuture resultFuture = resultFutureList.get(i);
			Object obj = resultFuture.take();
			if(!resultFuture.isSuccess()){
				logger.debug("[{}] job 결과 : {}", node, obj);
//				throw new FastcatSearchException("작업실패 collection="+collectionId+", "+node);
				return false;
			}
		}
		return true;
	}
}
