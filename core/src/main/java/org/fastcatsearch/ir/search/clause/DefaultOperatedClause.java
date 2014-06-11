package org.fastcatsearch.ir.search.clause;

import java.io.PrintStream;

import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.query.RowExplanation;

/**
 * */
public class DefaultOperatedClause extends OperatedClause {
	
	private OperatedClause operatedClause;
	private String description;
	
	public DefaultOperatedClause(OperatedClause operatedClause, String description) {
		super("DEFAULT");
		this.operatedClause = operatedClause;
		this.description = description;
	}
	
	@Override
	protected boolean nextDoc(RankInfo docInfo) {
		if(operatedClause == null){
			return false;
		}
		
		if(operatedClause.next(docInfo)){
			if(docInfo.rowExplanations() != null) {
				docInfo.rowExplanations().add(new RowExplanation(id, docInfo.score(), description));
			}
			return true;
		}
		
		return false;
	}

	@Override
	public void close() {
		if(operatedClause != null){
			operatedClause.close();
		}
	}

	@Override
	protected void initClause(boolean explain) {
		operatedClause.init(explanation != null ? explanation.createSubExplanation() : null);
	}

	@Override
	public void printTrace(PrintStream os, int depth) {
		operatedClause.printTrace(os, depth);
	}

}
