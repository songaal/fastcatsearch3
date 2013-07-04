package org.fastcatsearch.ir.query2;

import static org.junit.Assert.*;

import org.junit.Test;

public class SearchQueryTest {

	@Test
	public void test() {
		
		String queryString = "ti(+중국의 빠른 ?모바일 -혁명)";
//		clauseString = "ti(+중국의 빠른 ?모바일 -혁명) +au(james -leech) -cate(12000)";
//		clauseString = "+ti(+중국의 빠른 ?모바일 -혁명) ?{au(james -leech) cate(12000)}";
		SearchQueryParser searchQueryParser = new SearchQueryParser(queryString);
		
		SearchQuery searchQuery = searchQueryParser.generate();
		
		
		
	}

}
