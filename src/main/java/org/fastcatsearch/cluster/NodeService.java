package org.fastcatsearch.cluster;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.control.JobService;
import org.fastcatsearch.control.ResultFuture;
import org.fastcatsearch.env.Environment;
import org.fastcatsearch.job.StreamableJob;
import org.fastcatsearch.service.AbstractService;
import org.fastcatsearch.service.ServiceException;
import org.fastcatsearch.service.ServiceManager;
import org.fastcatsearch.settings.Settings;
import org.fastcatsearch.transport.TransportException;
import org.fastcatsearch.transport.TransportModule;
import org.fastcatsearch.transport.common.SendFileResultFuture;

public class NodeService extends AbstractService {

	private static NodeService instance;
	private TransportModule transportModule;
	private Node myNode;
	private Node masterNode;
	private List<Node> nodeList;
	
	public static NodeService getInstance(){
		return instance;
	}
	
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
		
		nodeList = new ArrayList<Node>();
		List<Settings> nodeSettingList = settings.getSettingList("node_list");
		for (int i = 0; i < nodeSettingList.size(); i++) {
			Settings nodeSetting = nodeSettingList.get(i);
			String id = nodeSetting.getString("id");
			String address = nodeSetting.getString("address");
			int port = nodeSetting.getInt("port");
			boolean isEnabled = !nodeSetting.getBoolean("disabled");
			boolean isMe = myNodeName.equals(id);
			boolean isMaster = masterNodeName.equals(id);
			
			Node node = new Node(id, address, port);
			nodeList.add(node);
			
			if(isEnabled){
				node.setEnabled();
			}else{
				node.setDisabled();
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
		
		for(Node node : nodeList){
			if(node.isEnabled() && !node.equals(myNode)){
				try {
					transportModule.connectToNode(node);
					node.setActive();
				} catch (TransportException e) {
					node.setInactive();
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
	public List<Node> getNodeList(){
		return null;
	}
	
	public Node getNodeById(String id){
		for(Node node : nodeList){
//			logger.debug("find node >> {}:{}", node.id(), id);
			if(node.id().equals(id)){
				return node;
			}
		}
		return null;
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
	
	public ResultFuture sendRequest(final Node node, final StreamableJob streamableJob) {
		try{
			return transportModule.sendRequest(node, streamableJob);
		}catch(TransportException e){
			logger.error("sendRequest 에러", e);
		}
		return null;
	}
	
	public SendFileResultFuture sendFile(final Node node, File sourcefile, File targetFile) {
		try{
			return transportModule.sendFile(node, sourcefile, targetFile);
		}catch(TransportException e){
			logger.error("sendFile 에러", e);
		}
		return null;
		
	}

}
