package org.fastcatsearch.ir.query;

import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.clause.Clause;

public abstract class QueryModifier extends Query {
	protected Query query;
	
	public QueryModifier(){
	}
	
	public QueryModifier modify(Query query){
		this.query = query;
		init0();
		return this;
	}
	
	protected abstract void init0();	
	
	@Override
	public Clause getClause(){
		return query.getClause();
	}

	@Override
	public ViewContainer getViews() {
		return query.getViews();
	}

	@Override
	public Filters getFilters() {
		return query.getFilters();
	}

	@Override
	public Groups getGroups() {
		return query.getGroups();
	}
	
	@Override
	public Sorts getSorts() {
		return query.getSorts();
	}

	@Override
	public Metadata getMeta() {
		return query.getMeta();
	}

	@Override
	public Filters getGroupFilters() {
		return query.getGroupFilters();
	}
}
