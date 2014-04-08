package org.fastcatsearch.ir.query;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.clause.Clause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QueryModifier extends Query {
	protected static Logger logger = LoggerFactory.getLogger(QueryModifier.class);

	protected Query query;

	public QueryModifier() {
	}

	public QueryModifier modify(Query query) throws IRException {
		this.query = query;
		init0();
		return this;
	}

	protected abstract void init0();

	@Override
	public Clause getClause() {
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
