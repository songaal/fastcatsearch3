package org.fastcatsearch.ir.query;

public class EmptyOperatedClause implements OperatedClause {

	@Override
	public boolean next(RankInfo docInfo) {
		return false;
	}

}
