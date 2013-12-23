package org.fastcatsearch.ir.search.clause;

import org.fastcatsearch.ir.query.RankInfo;

public class EmptyOperatedClause implements OperatedClause {

	@Override
	public boolean next(RankInfo docInfo) {
		return false;
	}

	@Override
	public void close() {
		
	}

}
