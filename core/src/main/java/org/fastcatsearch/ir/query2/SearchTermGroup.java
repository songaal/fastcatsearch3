package org.fastcatsearch.ir.query2;

import org.fastcatsearch.ir.query2.SearchQueryTerm.OCCUR;

public class SearchTermGroup extends SearchQueryEntry {
	public SearchTermGroup(SearchQueryEntry... termList){
		//OCCUR생략시 AND임. 
	}

	public SearchTermGroup(OCCUR must, SearchQueryEntry... termList) {
	}
}
