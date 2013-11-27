package org.fastcatsearch.cluster;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;

public class NodePingJob extends Job implements Streamable {

	private static final long serialVersionUID = -990602244092656595L;
	
	private String nodeId;
	
	public NodePingJob(){
	}
	public NodePingJob(String nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node node = nodeService.getNodeById(nodeId);
		if(node != null){
			if(!node.isActive()){
				logger.info("Node {} is set Active!", node);
				node.setActive();
			}
		}
		
		return new JobResult();
	}
	@Override
	public void readFrom(DataInput input) throws IOException {
		nodeId = input.readString();
	}
	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(nodeId);
	}

}
