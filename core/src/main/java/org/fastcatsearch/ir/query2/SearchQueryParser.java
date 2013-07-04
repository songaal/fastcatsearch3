package org.fastcatsearch.ir.query2;


public class SearchQueryParser {
	public SearchQueryParser(String queryString){
		
	}
	
	public SearchQuery generate(){
		
		//TODO 로직에 의해서 queryString을 파싱해서 term끼리 묶어준다.
		
		SearchQueryTerm term1 = new SearchQueryTerm(SearchQueryTerm.OCCUR.MUST, "");
		SearchQueryTerm term2 = new SearchQueryTerm(SearchQueryTerm.OCCUR.MUST, "");
		
		SearchTermGroup searchTermGroup = new SearchTermGroup(SearchQueryTerm.OCCUR.SHOULD, term1, term2);
		
		SearchQueryField field1 = new SearchQueryField(SearchQueryTerm.OCCUR.SHOULD, searchTermGroup);
		
		SearchQueryTerm term3 = new SearchQueryTerm(SearchQueryTerm.OCCUR.SHOULD, "");
		SearchQueryTerm term4 = new SearchQueryTerm(SearchQueryTerm.OCCUR.NOT, "");
		
		SearchTermGroup searchTermGroup2 = new SearchTermGroup(term3, term4);
		
		SearchQueryField field2 = new SearchQueryField(SearchQueryTerm.OCCUR.SHOULD, searchTermGroup2);
		
		//최종 필드들이 들어오면 OCCUR는 정의하지 않아도 됨. 결국 안쓰이게 됨.
		SearchTermGroup searchTermGroupFinal = new SearchTermGroup(field1, field2);
		
		return null;
	}
	
}
