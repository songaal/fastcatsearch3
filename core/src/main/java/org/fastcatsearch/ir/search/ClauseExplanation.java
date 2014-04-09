package org.fastcatsearch.ir.search;

import java.util.ArrayList;
import java.util.List;


public class ClauseExplanation {
	
	private String id; //필드아이디.또는 Operator
	private String term; //검색어 full term
	private int rows;
	private long time;
	
	private List<ClauseExplanation> subExplanations;
	
	public ClauseExplanation(){
	}
	
	public ClauseExplanation(String id, String term){
		this.id = id;
		this.term = term;
	}
	
	public ClauseExplanation createSubExplanation(){
		if(subExplanations == null){
			subExplanations = new ArrayList<ClauseExplanation>(2);
		}
		ClauseExplanation sub = new ClauseExplanation();
		subExplanations.add(sub);
		return sub;
	}
	
	public List<ClauseExplanation> getSubExplanations() {
		return subExplanations;
	}
	
	public String toString(){
		return getResultInfo(0);
	}
	public String getResultInfo(int depth){
		StringBuffer sb = new StringBuffer();
		String indent = "";
		for (int i = 0; i < depth; i++) {
			indent += "      ";
		}
		sb.append("<").append(id).append(">").append(term != null ? term : "").append(" rows[").append(rows).append("] time[").append(time).append("]");
		if (subExplanations != null) {
			for(ClauseExplanation exp : subExplanations){
				sb.append("\n").append(indent).append("   |--");
				sb.append(exp.getResultInfo(depth + 1));
			}
		}
		return sb.toString();
	}
	
//	public String getRowInfo(int depth){
//		StringBuffer sb = new StringBuffer();
//		String indent = "";
//		for (int i = 0; i < depth; i++) {
//			indent += "      ";
//		}
//		sb.append("[").append(id).append("]").append(term != null ? term : "").append(" score[").append(score).append("] weight[").append(weight).append("]");
//		if (subExplanations != null) {
//			for(ClauseExplanation exp : subExplanations){
//				sb.append("\n").append(indent).append("   |--");
//				sb.append(exp.getRowInfo(depth + 1));
//			}
//		}
//		return sb.toString();
//	}
	
	public void addTime(long t){
		time += t;
	}
	public void addRow(){
		rows += 1;
	}
	
//	public void set(int score, int tf){
//		this.score = score;
//		this.tf = tf;
//	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
//	public float getWeight() {
//		return weight;
//	}
//	public void setTerm(float weight) {
//		this.weight = weight;
//	}
//	public int getScore() {
//		return score;
//	}
//	public void setScore(int score) {
//		this.score = score;
//	}
//	public int getTf() {
//		return tf;
//	}
//	public void setTf(int tf) {
//		this.tf = tf;
//	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
}
