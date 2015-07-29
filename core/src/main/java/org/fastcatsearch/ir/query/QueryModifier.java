package org.fastcatsearch.ir.query;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.search.clause.Clause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QueryModifier extends Query {
	protected static Logger logger = LoggerFactory.getLogger(QueryModifier.class);

	protected String collectionId;
	protected Query query;

	public QueryModifier() {
	}

	public QueryModifier modify(String collectionId, Query query) throws IRException {
		this.collectionId = collectionId;
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
	
	@Override
	public Query getBoostQuery() {
		return query.getBoostQuery();
	}

	@Override
	public Bundle getBundle() {
		return query.getBundle();
	}

    @Override
    public String toString() {
        return query.toString();
    }
}
