package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.query.Query;

public interface SearchStatistics {
	public static final String KEYWORD = "Keyword";
	public static final String PREV_KEYWORD = "PrevKeyword";
	public static final String CATEGORY = "Category";
	
	public void add(Query q);
}
