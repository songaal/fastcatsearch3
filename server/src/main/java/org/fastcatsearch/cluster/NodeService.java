package org.fastcatsearch.cluster;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.job.cluster.NodeListUpdateJob;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.NodeListSettings;
import org.fastcatsearch.settings.NodeListSettings.NodeSettings;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.transport.TransportException;
import org.fastcatsearch.transport.TransportModule;
import org.fastcatsearch.transport.common.SendFileResultFuture;

public class NodeService extends AbstractService implements NodeLoadBalancable {
	
	private static NodeLoadBalancer loadBalancer;
	
	private TransportModule transportModule;
	private Node myNode;
	private Node masterNode;
	private List<Node> nodeList;
	private Map<String, Node> nodeMap;

	public NodeService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		JobService jobService = serviceManager.getService(JobService.class);

		String myNodeId = environment.myNodeId();
		String masterNodeId = environment.masterNodeId();

		nodeList = new ArrayList<Node>();
		nodeMap = new HashMap<String, Node>();
		NodeListSettings nodeListSettings = environment.settingManager().getNodeListSettings();
		if(nodeListSettings != null){
			for(NodeSettings nodeSetting : nodeListSettings.getNodeList()){
				String id = nodeSetting.getId();
				String name = nodeSetting.getName();
				String address = nodeSetting.getAddress();
				int port = nodeSetting.getPort();
				boolean isEnabled = nodeSetting.isEnabled();

				Node node = new Node(id, name, address, port, isEnabled);
				nodeList.add(node);
				nodeMap.put(id, node);
				
				if (isEnabled) {
					node.setEnabled();
				} else {
					node.setDisabled();
				}

				if(myNodeId.equals(id)){
					myNode = node;
				}

				if (masterNodeId.equals(id)) {
					masterNode = node;
				}
			}
		}

		if (myNode == null) {
			throw new FastcatSearchException("ERR-00300");
		}
		if (masterNode == null) {
			throw new FastcatSearchException("ERR-00301");
		}

		//자기자신은 active하다.
		myNode.setActive();
		
		transportModule = new TransportModule(environment, settings.getSubSettings("transport"), myNode.port(), jobService);
//		if (myNode.port() > 0) {
//			transportModule.settings().put("node_port", myNode.port());
//		}

		if (!transportModule.load()) {
			throw new FastcatSearchException("ERR-00305");
		}

		for (Node node : nodeList) {
			if (node.isEnabled() && !node.equals(myNode)) {
				try {
					transportModule.connectToNode(node);
					node.setActive();
					transportModule.sendRequest(node, new NodePingJob(myNode.id()));
				} catch (TransportException e) {
					logger.error("Cannot connect to {}", node);
					node.setInactive();
				}
			}
		}

		loadBalancer = new NodeLoadBalancer();
		
		return true;
	}

	@Override
	protected boolean doStop() throws FastcatSearchException {
		return transportModule.unload();
	}

	@Override
	protected boolean doClose() throws FastcatSearchException {
		return false;
	}

	public List<Node> getNodeList() {
		return nodeList;
	}

	public Node getNodeById(String id) {
		return nodeMap.get(id);
	}
	
	public List<Node> getNodeById(List<String> nodeIdList) {
		if(nodeIdList == null){
			return new ArrayList<Node>(); 
		}
		List<Node> result = new ArrayList<Node>(nodeIdList.size());
		for (String nodeId : nodeIdList) {
			result.add(nodeMap.get(nodeId));
		}
		return result;
	}

	public Node getMyNode() {
		return myNode;
	}

	public Node getMaserNode() {
		return masterNode;
	}

	public boolean isMaster() {
		if (masterNode != null && myNode != null) {
			return myNode.equals(masterNode);
		}
		return false;
	}

	public boolean isMyNode(Node node) {
		return myNode.equals(node);
	}

	public ResultFuture sendRequestToMaster(final Job job) {
		if (masterNode.equals(myNode)) {
			return JobService.getInstance().offer(job);
		}
		try {
			return transportModule.sendRequest(masterNode, job);
		} catch (TransportException e) {
			logger.error("sendRequest 에러 : {}", e.getMessage());
		}
		return null;
	}

	public ResultFuture sendRequest(final Node node, final Job job) {
		if (node.equals(myNode)) {
			return JobService.getInstance().offer(job);
		}
		try {
			return transportModule.sendRequest(node, job);
		} catch (TransportException e) {
			logger.error("sendRequest 에러 : {}", e.getMessage());
		}
		return null;
	}

	/*
	 * 파일만 전송가능. 디렉토리는 전송불가. 
	 * 동일노드로는 전송불가.
	 */
	public SendFileResultFuture sendFile(final Node node, File sourcefile, File targetFile) throws TransportException {
		//노드가 같고, file도 같다면 전송하지 않는다.
		if (node.equals(myNode)) {
			return null;
		}
		if (sourcefile.isDirectory()) {
			return null;
		}
		return transportModule.sendFile(node, sourcefile, targetFile);
	}

	@Override
	public void updateLoadBalance(String shardId, List<String> dataNodeIdList) {
		List<Node> list = getNodeById(dataNodeIdList);
		loadBalancer.update(shardId, list);
	}
	
	@Override
	public Node getBalancedNode(String shardId){
		Node node = loadBalancer.getBalancedNode(shardId);
		logger.debug("#Balanced node [{}] >> {}", shardId, node);
		return node;
	}
	
	public void updateNode(NodeListSettings nodeListSettings) {
		
		List<NodeSettings> nodeSettingList = nodeListSettings.getNodeList();

		for(int inx=0;inx< nodeSettingList.size();inx++) {
			NodeSettings setting = nodeSettingList.get(inx);

			String nodeId = setting.getId();
			String name = setting.getName();
			String address = setting.getAddress();
			int port = setting.getPort();
			boolean enabled = setting.isEnabled();

			Node node = null;

			if(nodeMap.containsKey(nodeId)) {
				node = nodeMap.get(nodeId);
			} else {
				//신규노드
				node = new Node(nodeId, name, address, port, enabled);
				nodeMap.put(nodeId, node);
				nodeList.add(node);
			}
		}
		
		//settingManager.storeNodeListSettings(nodeListSettings);
	}
}
