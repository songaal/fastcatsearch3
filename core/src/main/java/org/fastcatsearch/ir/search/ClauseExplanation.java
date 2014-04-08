package org.fastcatsearch.ir.search;


public class ClauseExplanation {
	
	//공통정보.
	private String id; //필드아이디.또는 Operator
	private String term; //검색어 full term
	private float weight;
	
	//결과 1건씩 통계. 매번변경.
	private int score; //점수.
	private int tf; // tf
	
	//전체통계.
	private int rows;
	private long time;
	
	private ClauseExplanation sub1;
	private ClauseExplanation sub2;
	
	public ClauseExplanation(){
	}
	
//	public ClauseExplanation(String id){
//		this.id = id;
//	}
	
	public ClauseExplanation(String id, String term){
		this.id = id;
		this.term = term;
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
		if (sub1 != null) {
			sb.append("\n").append(indent).append("   |--");
			sb.append(sub1.getResultInfo(depth + 1));
		}
		if (sub2 != null) {
			sb.append("\n").append(indent).append("   |--");
			sb.append(sub2.getResultInfo(depth + 1));
		}
		
		return sb.toString();
	}
	
	public String getRowInfo(int depth){
		StringBuffer sb = new StringBuffer();
		String indent = "";
		for (int i = 0; i < depth; i++) {
			indent += "      ";
		}
		sb.append("[").append(id).append("]").append(term != null ? term : "").append(" score[").append(score).append("] weight[").append(weight).append("]");
		if (sub1 != null) {
			sb.append("\n").append(indent).append("   |--");
			sb.append(sub1.getRowInfo(depth + 1));
		}
		if (sub2 != null) {
			sb.append("\n").append(indent).append("   |--");
			sb.append(sub2.getRowInfo(depth + 1));
		}
		
		return sb.toString();
	}
	
	public void addTime(long t){
		time += t;
	}
	public void addRow(){
		rows += 1;
	}
	
	public void set(int score, int tf){
		this.score = score;
		this.tf = tf;
	}
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
	public float getWeight() {
		return weight;
	}
	public void setTerm(float weight) {
		this.weight = weight;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getTf() {
		return tf;
	}
	public void setTf(int tf) {
		this.tf = tf;
	}
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
	public ClauseExplanation getSub1() {
		return sub1;
	}
	public void setSub1(ClauseExplanation sub1) {
		this.sub1 = sub1;
	}
	public ClauseExplanation getSub2() {
		return sub2;
	}
	public void setSub2(ClauseExplanation sub2) {
		this.sub2 = sub2;
	}

	public ClauseExplanation createSub1() {
		sub1 = new ClauseExplanation();
		return sub1; 
	}
	
	public ClauseExplanation createSub2() {
		sub2 = new ClauseExplanation();
		return sub2; 
	}
	
}
