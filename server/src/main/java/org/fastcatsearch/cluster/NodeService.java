package org.fastcatsearch.cluster;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.exception.FastcatSearchException;
import org.fastcatsearch.job.Job;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.NodeListSettings;
import org.fastcatsearch.settings.NodeListSettings.NodeSettings;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.transport.TransportException;
import org.fastcatsearch.transport.TransportModule;
import org.fastcatsearch.transport.common.SendFileResultFuture;
import org.fastcatsearch.util.FileUtils;

public class NodeService extends AbstractService implements NodeLoadBalancable {
	
	private static NodeLoadBalancer loadBalancer;
	
	private TransportModule transportModule;
	private Node myNode;
	private Node masterNode;
	private Map<String, Node> nodeMap;

	public NodeService(Environment environment, Settings settings, ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	@Override
	protected boolean doStart() throws FastcatSearchException {
		JobService jobService = serviceManager.getService(JobService.class);

		String myNodeId = environment.myNodeId();
		String masterNodeId = environment.masterNodeId();

		nodeMap = new HashMap<String, Node>();
		NodeListSettings nodeListSettings = environment.settingManager().getNodeListSettings();
		if(nodeListSettings != null){
			for(NodeSettings nodeSetting : nodeListSettings.getNodeList()){
				String id = nodeSetting.getId();
				boolean isEnabled = nodeSetting.isEnabled();

				Node node = new Node(nodeSetting);
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
		int servicePort = environment.settingManager().getIdSettings().getInt("servicePort");
		myNode.setServicePort(servicePort);

		boolean hasSeparateDataNetwork = myNode.dataAddress() != null;
		transportModule = new TransportModule(environment, settings.getSubSettings("transport"), myNode.port(), jobService, hasSeparateDataNetwork);
		if (!transportModule.load()) {
			throw new FastcatSearchException("ERR-00305");
		}

		NodeHandshakeJob nodeHandshakeJob = new NodeHandshakeJob(myNode.id(), servicePort);

		for (Node node : nodeMap.values()) {
			if (node!=null && node.isEnabled() && !node.equals(myNode)) {
				try {
					transportModule.connectToNode(node);
					node.setActive();
					transportModule.sendRequest(node, nodeHandshakeJob);
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
		return true;
	}

	public Collection<Node> getNodeList() {
		return nodeMap.values();
	}
	
	public List<Node> getNodeArrayList() {
		return new ArrayList<Node>(nodeMap.values());
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

	public Node getMasterNode() {
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
		if(node == null || job == null){
			return null;
		}
		
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
		if (sourcefile.isDirectory()) {
			return null;
		}
		
		//노드가 같고, file도 같다면 전송하지 않는다.
		if (node.equals(myNode)) {
			File a = environment.filePaths().makePath(sourcefile.getPath()).file();
			File b = environment.filePaths().makePath(targetFile.getPath()).file();
//			logger.warn("compare. [{}] : [{}] = {}", a.getAbsolutePath(), b.getAbsolutePath(), a.getAbsolutePath().equals(b.getAbsolutePath()));
			
			if(a.getAbsolutePath().equals(b.getAbsolutePath())){
				logger.warn("Cannot send same file to same node. Skip! {}", sourcefile);
				return null;
			}else{
				//다르다면 로컬 복사한다.
				try {
					targetFile = environment.filePaths().makePath(targetFile.getPath()).file();
					logger.warn("Copy files locally. {} > {}", sourcefile.getAbsolutePath(), targetFile.getAbsolutePath());
					FileUtils.copyFile(sourcefile, targetFile);
					SendFileResultFuture f = new SendFileResultFuture(0, null);
					f.put(null, true);
					return f;
				} catch (IOException e) {
					throw new TransportException("Fail to copy local file. " + sourcefile + "> " + targetFile);
				}
			}
		}
		
		
		return transportModule.sendFile(node, sourcefile, targetFile);
	}

	@Override
	public void updateLoadBalance(String collectionId, List<String> dataNodeIdList) {
		List<Node> list = getNodeById(dataNodeIdList);
		loadBalancer.update(collectionId, list);
	}
	
	@Override
	public Node getBalancedNode(String collectionId){
		Node node = loadBalancer.getBalancedNode(collectionId);
		logger.debug("#Balanced node [{}] >> {}", collectionId, node);
		return node;
	}
	
	/**
	 * FIXME:노드 정지 및 삭제에 대한 기능이 들어있지 않음.
	 * @param nodeListSettings
	 */
	public void updateNode(NodeListSettings nodeListSettings) {
		
		List<NodeSettings> nodeSettingList = nodeListSettings.getNodeList();
		
		logger.trace("updating node..");
		
//		if(nodeSettingList.size() < nodeMap.size()) {
			//삭제된 경우.
		Iterator<Entry<String, Node>> iterator = nodeMap.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<String, Node> entry = iterator.next();
				//못찾으면 삭제된 것이다.
				if(nodeListSettings.findNodeById(entry.getKey())==-1) {
					//노드 삭제
					iterator.remove();
//					nodeMap.remove(key);
				}
			}
			
//		} else {
		
			for(int inx=0;inx< nodeSettingList.size();inx++) {
				NodeSettings setting = nodeSettingList.get(inx);
	
				String nodeId = setting.getId();
				String name = setting.getName();
				boolean enabled = setting.isEnabled();
				InetSocketAddress address = new InetSocketAddress(setting.getAddress(), setting.getPort());
	
				Node node = null;
	
				if(nodeMap.containsKey(nodeId)) {
					node = nodeMap.get(nodeId);
					//주소 및 포트가 변경 되었다면
					logger.debug("check node {} ({}:{}) : ({}:{})", nodeId, address.getAddress(), address.getPort(), node.address().getAddress(), node.address().getPort());
					Node newNode = new Node(setting);
					nodeMap.put(nodeId, newNode);
					if (!(address.getAddress().getHostAddress() != null
							&& address.getAddress().getHostAddress().equals(node.address().getAddress().getHostAddress())
							&& address.getPort() == node.address().getPort()
						)) {
						//기존 노드의 삭제 후 새 노드를 시작함.
						logger.debug("updating node {} ({}/{}:{})..", inx, nodeId, node.address(), node.port());
					}else {
						if(node.isActive()) {
							newNode.setActive();
						}
					}
				} else {
					//노드추가
					node = new Node(setting);
					logger.debug("add new node.. {}", node);
					nodeMap.put(nodeId, node);
				}
			}
//		}
		environment.settingManager().storeNodeListSettings(nodeListSettings);
	}
}