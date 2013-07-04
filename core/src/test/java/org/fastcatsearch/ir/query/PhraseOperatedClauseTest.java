package org.fastcatsearch.ir.query;

import org.fastcatsearch.ir.search.CompositeTermDoc;
import org.junit.Test;

public class PhraseOperatedClauseTest {

	@Test
	public void test() {
		MultiTermOperatedClause phraseOperatedClause = new MultiTermOperatedClause();
		
		CompositeTermDoc termDocs1 = null;//new TermDocs(0, new CharVector("abc"), 5, new int[]{1,2,3,4,7,8}, new int[]{1,1,2,2,1});
		CompositeTermDoc termDocs2 = null;//new TermDocs(0, new CharVector("def"), 5, new int[]{2,3,4,5,6,8}, new int[]{1,1,2,2,1});
		CompositeTermDoc termDocs3 = null;//new TermDocs(0, new CharVector("ghi"), 5, new int[]{2,3,4,5,6,8}, new int[]{1,1,2,2,1});
		
		int queryPosition = 0;
		
		phraseOperatedClause.addTerm(termDocs1, queryPosition++);
		phraseOperatedClause.addTerm(termDocs2, queryPosition++);
		phraseOperatedClause.addTerm(termDocs3, queryPosition++);
		
		RankInfo rankInfo = new RankInfo();
		while(phraseOperatedClause.next(rankInfo)){
			System.out.println(rankInfo);
		}
		
	}

}
