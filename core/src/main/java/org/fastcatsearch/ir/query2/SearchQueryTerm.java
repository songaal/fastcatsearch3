package org.fastcatsearch.ir.query2;


/**
 * 검색 구문을 담당.
 * 
 * */
public class SearchQueryTerm extends SearchQueryEntry {
	
	public static enum OCCUR { MUST, SHOULD, NOT }
	
	public SearchQueryTerm(String termString){
		
	}

	public SearchQueryTerm(OCCUR must, String string) {
	}
	
}
