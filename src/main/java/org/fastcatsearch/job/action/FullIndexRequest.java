package org.fastcatsearch.job.action;

import java.util.List;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.control.JobException;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.data.DataService;
import org.fastcatsearch.data.DataStrategy;
import org.fastcatsearch.job.IndexingJob;
import org.fastcatsearch.job.NodeFullIndexJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.service.ServiceException;

public class FullIndexRequest extends IndexingJob {

	@Override
	public JobResult doRun() throws JobException, ServiceException {
		
		String[] args = getStringArrayArgs();
		String collectionId = (String)args[0];
		DataStrategy dataStrategy = DataService.getInstance().getCollectionDataStrategy(collectionId);
		List<Node> nodeList = dataStrategy.indexNodes();
		if(nodeList == null || nodeList.size() == 0){
			throw new JobException("색인할 노드가 정의되어있지 않습니다.");
		}
		
		//
		//TODO 어느 노드로 색인할지 고른다.
		//현 버전에서는 일단 첫번째 노드로 색인.
		
		//선택된 노드로 색인 메시지를 전송한다.
		Node node =  nodeList.get(0);
		long st = System.currentTimeMillis();
		NodeFullIndexJob job = new NodeFullIndexJob(collectionId);
		ResultFuture resultFuture = NodeService.getInstance().sendRequest(node, job);
		long et = System.currentTimeMillis();
		Object result = resultFuture.take();
		if(!resultFuture.isSuccess()){
			if(result instanceof Throwable){
				throw new JobException("색인노드에서 전체색인중 에러발생.", (Throwable) result);
			}else{
				throw new JobException("색인노드에서 전체색인중 에러발생.");
			}
		}

		IndexingJobResult indexingJobResult = (IndexingJobResult) result;
		
		return new JobResult(indexingJobResult);
	}

}
