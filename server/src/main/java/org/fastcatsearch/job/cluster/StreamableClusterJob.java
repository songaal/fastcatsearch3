package org.fastcatsearch.job.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.StreamableJob;

public class StreamableClusterJob extends StreamableJob {

	private static final long serialVersionUID = 2080251241965769963L;

	@Override
	public void readFrom(DataInput input) throws IOException {
	}

	@Override
	public void writeTo(DataOutput output) throws IOException {
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		return null;
	}
	
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
