package org.fastcatsearch.statistics.log;

public class SearchLog extends AbstractLog {
	private String prevKeyword;

	public SearchLog(String keyword, String prevKeyword) {
		super(keyword);
		this.prevKeyword = prevKeyword;
	}

	public String getPrevKeyword() {
		return prevKeyword;
	}

	public String toString() {
		return getClass().getSimpleName() + ": " + getKey() + " : " + prevKeyword;
	}
}
