package org.fastcatsearch.node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fastcatsearch.cluster.Node;
import org.fastcatsearch.ir.config.CollectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReloadNode {
	private Logger logger = LoggerFactory.getLogger(ReloadNode.class);

	private Map<String, Set<Node>> collectionCopyNode;
	private Map<String, CollectionContext> collectionContext;

	private ReloadNode(){
		if(this.collectionCopyNode == null){
			this.collectionCopyNode = new HashMap<String, Set<Node>>();
		}

		if(this.collectionContext == null){
			this.collectionContext = new HashMap<String, CollectionContext>();
		}
	}

	private static class LazyHolder{
		public static final ReloadNode INSTANCE = new ReloadNode();
	}

	public static ReloadNode getInstance() {
		return LazyHolder.INSTANCE;
	}

	public void init(String collectionId){
		collectionCopyNode.remove(collectionId);
		collectionContext.remove(collectionId);
		
		logger.debug(">> collectionId is {} remove.", collectionId);		
		logger.debug(">> copyNode size {}, collectionContext size {} ", collectionCopyNode.size(), collectionContext.size());
	}

	public boolean  isNullCheck(String collectionId){
		if(collectionCopyNode.containsKey(collectionId) && collectionContext.containsKey(collectionId)){			
			return false;	
		}

		return true;
	}

	/**
	 * @return the collectionCopyNode
	 */
	public Map<String, Set<Node>> getCollectionCopyNode() {
		return collectionCopyNode;
	}

	/**
	 * @param collectionCopyNode the collectionCopyNode to set
	 */
	public void setCollectionCopyNode(Map<String, Set<Node>> collectionCopyNode) {
		this.collectionCopyNode = collectionCopyNode;
	}

	public void putCollectionCopyNode(String collectionId, Set<Node> nodeSet){
		this.collectionCopyNode.put(collectionId, nodeSet);		
	}
	
	/**
	 * @return the collectionContext
	 */
	public Map<String, CollectionContext> getCollectionContext() {
		return collectionContext;
	}

	/**
	 * @param collectionContext the collectionContext to set
	 */
	public void setCollectionContext(Map<String, CollectionContext> collectionContext) {
		this.collectionContext = collectionContext;
	}

	public void putCollectionContext(String collectionId, CollectionContext collectionContext){
		this.collectionContext.put(collectionId, collectionContext);
	}
}
