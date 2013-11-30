package org.fastcatsearch.cluster;

public class NodeJobResult {
	
	private Node node;
	private Object result;
	private boolean isSuccess;
	
	public NodeJobResult(Node node, Object result, boolean isSuccess) {
		this.node = node;
		this.result = result;
		this.isSuccess = isSuccess;
	}
	
	public Node node(){
		return node;
	}
	
	public Object result(){
		return result;
	}

	public boolean isSuccess(){
		return isSuccess;
	}
	
	public String toString(){
		return getClass().getSimpleName() + "] " + node + " : " + isSuccess + " : " + result; 
	}
}
