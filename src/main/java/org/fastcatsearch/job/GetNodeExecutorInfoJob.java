package org.fastcatsearch.job;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;

import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.service.ServiceManager;

public class GetNodeExecutorInfoJob extends Job {

	@Override
	public JobResult doRun() throws FastcatSearchException {
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node node = nodeService.getNodeById("chaos");
		ResultFuture resultFuture = nodeService.sendRequest(node, new MonitorJobExecutorStreamableJob());
		Object result = resultFuture.take();
		if(resultFuture.isSuccess()){
			return new JobResult(result);
		}else{
			throw new FastcatSearchException("결과실패 >> "+result);
		}
		
	}
	
}
