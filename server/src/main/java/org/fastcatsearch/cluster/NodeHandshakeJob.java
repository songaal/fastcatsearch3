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
	
	//서비스 포트정보도 함께 전달한다.
	private int servicePort;
	
	public NodeHandshakeJob(){
	}
	public NodeHandshakeJob(String nodeId, int servicePort) {
		this(nodeId, true, servicePort);
	}
	
	private NodeHandshakeJob(String nodeId, boolean isRequest, int servicePort) {
		this.nodeId = nodeId;
		this.isRequest = isRequest;
		this.servicePort = servicePort;
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
					node.setServicePort(servicePort);
					int myServicePort = environment.settingManager().getIdSettings().getInt("servicePort");
					//나의 정보를 답신으로 보낸다.
					nodeService.sendRequest(node, new NodeHandshakeJob(environment.myNodeId(), false, myServicePort));
				}else{
					node.setServicePort(servicePort);
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
		servicePort = input.readInt();
	}
	@Override
	public void writeTo(DataOutput output) throws IOException {
		output.writeString(nodeId);
		output.writeBoolean(isRequest);
		output.writeInt(servicePort);
	}

	
}
