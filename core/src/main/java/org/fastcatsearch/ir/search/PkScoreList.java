package org.fastcatsearch.ir.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PkScoreList extends ArrayList<PkScore> {

	private static final long serialVersionUID = 7372400924634916702L;
	private static Comparator<PkScore> comparator = new Comparator<PkScore>(){
		@Override
		public int compare(PkScore o1, PkScore o2) {
			return o1.getPk().compareTo(o2.getPk());
		}
	};
	
	private String keyword;
	public PkScoreList(String keyword) {
		this.keyword = keyword;
	}
	
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	
	public void sort(){
		Collections.sort(this, comparator);
	}
	
}
