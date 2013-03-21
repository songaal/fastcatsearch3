package org.fastcatsearch.cluster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.transport.TransportException;
import org.fastcatsearch.transport.TransportModule;

public class NodeService extends AbstractService {

	private static NodeService instance;
	private TransportModule transportModule;
	private Node myNode;
	private Node masterNode;
	private Map<String, Node> nodeMap;
	
	public NodeService(Environment environment, Settings settings,
			ServiceManager serviceManager) {
		super(environment, settings, serviceManager);
	}

	@Override
	public void asSingleton() {
		instance = this;
	}
	
	@Override
	protected boolean doStart() throws ServiceException {
		JobService jobService = serviceManager.getService(JobService.class);
		
		String myNodeName = settings.getString("me");
		String masterNodeName = settings.getString("master");
		
		nodeMap = new HashMap<String, Node>();
		List<Settings> nodeSettingList = settings.getSettingList("node_list");
		for (int i = 0; i < nodeSettingList.size(); i++) {
			Settings nodeSetting = nodeSettingList.get(i);
			String id = nodeSetting.getString("id");
			String address = nodeSetting.getString("address");
			int port = nodeSetting.getInt("port");
			boolean isActive = nodeSetting.getBoolean("active");
			boolean isMe = myNodeName.equals(id);
			boolean isMaster = masterNodeName.equals(id);
			
			Node node = new Node(id, address, port);
			
			if(isActive){
				nodeMap.put(id, node);
			}
			
			if(isMe){
				myNode = node;
			}
			
			if(isMaster){
				masterNode = node;
			}
			
			
		}
		
		if(myNode == null){
			throw new ServiceException("no my node found error!");
		}
		if(masterNode == null){
			throw new ServiceException("no master node found error!");
		}
		
		transportModule = new TransportModule(environment, settings, jobService);
		if(myNode.port() > 0 ){
			transportModule.settings().put("node_port", myNode.port());
		}
		
		if(!transportModule.load()){
			throw new ServiceException("can not load transport module!");
		}
		
		Iterator<Entry<String, Node>> nodeIterator = nodeMap.entrySet().iterator();
		while(nodeIterator.hasNext()){
			Entry<String, Node> entry = nodeIterator.next();
			Node node = entry.getValue();
			if(!node.equals(myNode)){
				try {
					transportModule.connectToNode(node);
				} catch (TransportException e) {
				}
			}
		}
		
		return true;
	}

	
	@Override
	protected boolean doStop() throws ServiceException {
		return transportModule.unload();
	}

	@Override
	protected boolean doClose() throws ServiceException {
		return false;
	}
	
	public Node getNode(String id){
		return nodeMap.get(id);
	}
	
	public Node getMyNode(){
		return myNode;
	}
	
	public Node getMaserNode(){
		return masterNode;
	}
	
	public boolean isMaster(){
		if(masterNode != null && myNode != null){
			return myNode.equals(masterNode);
		}
		return false;
	}

}
