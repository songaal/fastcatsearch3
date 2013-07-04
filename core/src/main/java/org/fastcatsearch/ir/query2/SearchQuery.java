package org.fastcatsearch.ir.query2;


/**
 * term들을 and/or로 묶어서 clause를 만들어준다.
 * */
public class SearchQuery {
	
	private SearchClause searchClause;
	private SearchQueryTerm term;
	
	
	

	
	class OrClause {
		public OrClause(SearchClause c1, SearchClause c2){
			
		}
	}
	
	class AndClause {
		public AndClause(SearchClause c1, SearchClause c2){
			
		}
	}
	
	class NotClause {
		
	}
	
	class LorClause {
		
	}
	
	class RorClause {
		
	}
}


