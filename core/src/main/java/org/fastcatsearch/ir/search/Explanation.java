package org.fastcatsearch.ir.search;

public class Explanation {
	private ClauseExplanation clauseExplanation;
	
	private String nodeId;
	private String collectionId;
	private int segmentId;
	
	public Explanation(){
	}
	
	public ClauseExplanation createClauseExplanation(){
		clauseExplanation = new ClauseExplanation();
		return clauseExplanation;
	}
	
	public void setClauseExplanation(ClauseExplanation clauseExplanation){
		this.clauseExplanation = clauseExplanation;
	}
	
	public ClauseExplanation clauseExplanation(){
		return clauseExplanation;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public void setSegmentId(int segmentId) {
		this.segmentId = segmentId;
	}
	
	public int getSegmentId() {
		return segmentId;
	}
	
	
}
