package org.fastcatsearch.ir.query;

import org.apache.lucene.analysis.Analyzer;

public class BooleanClause implements OperatedClause {

	
	public BooleanClause(Term term, Analyzer analyzer){
		String termString = term.termString();
		term.indexFieldId();
	}
	
	@Override
	public boolean next(RankInfo docInfo) {
		// TODO Auto-generated method stub
		return false;
	}

}
