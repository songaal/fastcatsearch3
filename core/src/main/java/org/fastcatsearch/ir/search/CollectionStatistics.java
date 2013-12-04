package org.fastcatsearch.ir.search;

import org.fastcatsearch.ir.query.Query;

public interface CollectionStatistics {
	
	public void start();
	public void stop();
	public void close();
	
	public void add(Query q);
}
