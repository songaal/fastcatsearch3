package org.fastcatsearch.ir.search;

public class PkScore {
	private String pk;
	private int score;
	
	public PkScore(String pk, int score) {
		this.pk = pk;
		this.score = score;
	}

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	
}
