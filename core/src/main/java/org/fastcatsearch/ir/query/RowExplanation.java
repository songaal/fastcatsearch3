package org.fastcatsearch.ir.query;


public class RowExplanation {
	private String id;
	private int score;
	private String description;
	
	public RowExplanation(String id, int score) {
		this(id, score, null);
	}
	
	public RowExplanation(String id, int score, String description) {
		this.id = id;
		this.score = score;
		this.description = description;
	}

	@Override
	public String toString(){
		return "[" + id+ "] " + score + " : " + description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
}
