package org.fastcatsearch.job;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.service.ServiceException;

public class GetNodeExecutorInfoJob extends Job {

	@Override
	public JobResult doRun() throws JobException, ServiceException {
		NodeService nodeService = NodeService.getInstance();
		Node node = nodeService.getNodeById("chaos");
		ResultFuture resultFuture = nodeService.sendRequest(node, new MonitorJobExecutorStreamableJob());
		Object result = resultFuture.take();
		if(resultFuture.isSuccess()){
			return new JobResult(result);
		}else{
			throw new JobException("결과실패 >> "+result);
		}
		
	}
	
}
