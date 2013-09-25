package org.fastcatsearch.job.indexing;

import org.fastcatsearch.cluster.ClusterStrategy;
import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.IRService;
import org.fastcatsearch.ir.common.IndexingType;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.job.result.IndexingJobResult;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.transport.vo.StreamableThrowable;

public class ClusterIndexingJob extends IndexingJob {

	private static final long serialVersionUID = 5314187715835186514L;

	private IndexingType indexingType;
	
	public ClusterIndexingJob(IndexingType indexingType){
		this.indexingType = indexingType;
	}
	
	@Override
	public JobResult doRun() throws FastcatSearchException {
		prepare(indexingType);
		
		updateIndexingStatusStart();
		
		boolean isSuccess = false;
		Object result = null;

		String collectionId = null;
		Throwable throwable = null;
		
		try {
			String[] args = getStringArrayArgs();
			collectionId = (String) args[0];

			NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
			IRService irService = ServiceManager.getInstance().getService(IRService.class);
//			DataService dataService = serviceManager.getService(DataService.class);
			ClusterStrategy dataStrategy = irService.getCollectionClusterStrategy(collectionId);
			String indexingNodeId = dataStrategy.indexingNode();
			Node indexingNode = nodeService.getNodeById(indexingNodeId);
					
			if (indexingNodeId == null) {
				throw new FastcatSearchException("색인할 노드가 정의되어있지 않습니다.");
			}

			//
			// TODO 어느 노드로 색인할지 고른다.
			// 현 버전에서는 일단 첫번째 노드로 색인.
			

			// 선택된 노드로 색인 메시지를 전송한다.
			StreamableJob indexingJob = null;
			if(indexingType == IndexingType.FULL){
				indexingJob = new IndexNodeFullIndexingJob(collectionId);
			}else if(indexingType == IndexingType.ADD){
				indexingJob = new IndexNodeAddIndexingJob(collectionId);
			}
			ResultFuture resultFuture = nodeService.sendRequest(indexingNode, indexingJob);
			result = resultFuture.take();
			isSuccess = resultFuture.isSuccess();

			if (!isSuccess) {
				if (result instanceof Throwable) {
					throw (Throwable) result;
				}
			}

			isSuccess = ((IndexingJobResult) result).isSuccess;
			
			return new JobResult(result);

		} catch (Throwable e) {
			throwable = e;
			throw new FastcatSearchException(throwable); // 전체색인실패.

		} finally {
			
			Streamable streamableResult = null;
			if (throwable != null) {
				streamableResult = new StreamableThrowable(throwable);
			} else if (result instanceof IndexingJobResult) {
				streamableResult = (IndexingJobResult) result;
			}


			updateIndexingStatusFinish(isSuccess, streamableResult);
			
		}
	}
}
