package org.fastcatsearch.job.cluster;

import java.util.List;

import org.fastcatsearch.cluster.NodeService;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;

public class SyncShardDataNodeJob extends Job {
	
	private static final long serialVersionUID = -9083556280027496514L;
	
	private String shardId;
	private List<String> dataNodeIdList;
	
	public SyncShardDataNodeJob(String shardId, List<String> dataNodeIdList){
		this.shardId = shardId;
		this.dataNodeIdList = dataNodeIdList;
	}
	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		//기존 Data node list 와 비교하여 이동시킨다. 
		
		//파일복사후 shard load. 
		
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		nodeService.updateLoadBalance(shardId, dataNodeIdList);
		
		return new JobResult();
	}

}
