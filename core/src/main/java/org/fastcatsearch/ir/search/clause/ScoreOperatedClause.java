package org.fastcatsearch.ir.search.clause;

import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.clause.OperatedClause;

/**
 * 검색된 posting에 대해서 hit수와 상관없이 동일한 점수(score)를 부여한다.
 * */
public class ScoreOperatedClause implements OperatedClause {
	
	private OperatedClause operatedClause;
	private int score;
	
	public ScoreOperatedClause(OperatedClause operatedClause, int score) {
		this.operatedClause = operatedClause;
		this.score = score;
	}

	@Override
	public boolean next(RankInfo docInfo) {
		if(operatedClause == null){
			return false;
		}
		
		boolean b = operatedClause.next(docInfo);
		if(b){
			docInfo.score(score);
		}
		return b;
	}

	@Override
	public void close() {
		if(operatedClause != null){
			operatedClause.close();
		}
	}

}
