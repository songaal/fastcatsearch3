//package org.fastcatsearch.data;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.fastcatsearch.cluster.Node;
//import org.fastcatsearch.cluster.NodeService;
//import org.fastcatsearch.env.Environment;
//import org.fastcatsearch.exception.FastcatSearchException;
//import org.fastcatsearch.service.AbstractService;
//import org.fastcatsearch.service.ServiceManager;
//import org.fastcatsearch.settings.Settings;
//
//public class DataService extends AbstractService {
//
//	private ClusterStrategy defaultDataStrategy;
//	private Map<String, ClusterStrategy> clusterStrategy;
//	
//	public DataService(Environment environment, Settings settings, ServiceManager serviceManager) {
//		super(environment, settings, serviceManager);
//	}
//
//	@Override
//	protected boolean doStart() throws FastcatSearchException {
//		List<String> dataNodeList = settings().getList("default.data_node", String.class);
//		List<String> indexNodeList = settings().getList("default.index_node", String.class);
//		int shardCount = settings().getInt("default.shard");
//		int replicaCount = settings().getInt("default.replica");
//		List<Node> dataNodes = getNodeList(dataNodeList);
//		List<Node> indexNodes = getNodeList(indexNodeList);
//		
//		defaultDataStrategy = new ClusterStrategy(dataNodes, indexNodes, shardCount, replicaCount);
//		
//		clusterStrategy = new ConcurrentHashMap<String, ClusterStrategy>();
//		List<Settings> list = settings().getSettingList("collection");
//		for (int i = 0; i < list.size(); i++) {
//			Settings settingNode = list.get(i);
//			String collectionId = settingNode.getString("id");
//			List<String> collectionDataNodeList = settingNode.getList("data_node", String.class);
//			List<String> collectionIndexNodeList = settingNode.getList("index_node", String.class);
//			List<Node> collectionDataNodes = null;
//			List<Node> collectionIndexNodes = null;
//			if(collectionDataNodeList != null){
//				collectionDataNodes = getNodeList(collectionDataNodeList);
//			}else{
//				collectionDataNodes = dataNodes;
//			}
//			if(collectionIndexNodeList != null){
//				collectionIndexNodes = getNodeList(collectionIndexNodeList);
//			}else{
//				collectionIndexNodes = indexNodes;
//			}
//			int collectionShardCount = settingNode.getInt("shard");
//			if(collectionShardCount == -1){
//				collectionShardCount = shardCount;
//			}
//			int collectionReplicaCount = settingNode.getInt("replica");
//			if(collectionReplicaCount == -1){
//				collectionReplicaCount = replicaCount;
//			}
//			
//			clusterStrategy.put(collectionId, new ClusterStrategy(collectionIndexNodes, collectionDataNodes, collectionShardCount, collectionReplicaCount));
//		}
//		
//		return true;
//	}
//
//	private List<Node> getNodeList(List<String> nodeIdList) {
//		List<Node> nodeList = new ArrayList<Node>();
//		NodeService nodeService = serviceManager.getService(NodeService.class);
//		for (int i = 0; i < nodeIdList.size(); i++) {
//			String nodeId = nodeIdList.get(i);
//			Node node = nodeService.getNodeById(nodeId);
//			if(node != null){
//				nodeList.add(node);
//			}
//			
//		}
//		return nodeList;
//	}
//
//	
//	public ClusterStrategy getCollectionDataStrategy(String collectionId){
//		ClusterStrategy dataStrategy =  clusterStrategy.get(collectionId);
//		if(dataStrategy == null){
//			dataStrategy = defaultDataStrategy;
//		}
//		
//		return dataStrategy;
//		
//	}
//	
//	@Override
//	protected boolean doStop() throws FastcatSearchException {
//		clusterStrategy.clear();
//		
//		return true;
//	}
//
//	@Override
//	protected boolean doClose() throws FastcatSearchException {
//		return true;
//	}
//
//}
