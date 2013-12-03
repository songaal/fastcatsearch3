package org.fastcatsearch.cluster;

import java.io.IOException;

import org.fastcatsearch.common.io.Streamable;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.ir.io.DataInput;
import org.fastcatsearch.ir.io.DataOutput;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.ServiceManager;

public class NodeHandshakeJob extends Job implements Streamable {

	private static final long serialVersionUID = -990602244092656595L;
	
	private String nodeId;
	private boolean isRequest;
	
	public NodeHandshakeJob(){
	}
	public NodeHandshakeJob(String nodeId) {
		this(nodeId, true);
	}
	private NodeHandshakeJob(String nodeId, boolean isRequest) {
		this.nodeId = nodeId;
		this.isRequest = isRequest;
	}

	@Override
	public JobResult doRun() throws FastcatSearchException {
		
		NodeService nodeService = ServiceManager.getInstance().getService(NodeService.class);
		Node node = nodeService.getNodeById(nodeId);
		if(node != null){
			if(node.isEnabled()){
				if(isRequest){
					//make connection if not exist.
					logger.info("Node {} is set Active!", node);
					node.setActive();
					nodeService.sendRequest(node, new NodeHandshakeJob(nodeId, false));
				}
			}else{
				logger.info("Node {} is disabled! Cannot accept node request.", node);
			}
		}
		
		return new JobResult();
	}
	@Override
	public void readFrom(DataInput input) throws IOException {
		nodeId = input.readString();
		isRequest = input.readBoolean();
	}
	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(nodeId);
		output.writeBoolean(isRequest);
	}

	
}
