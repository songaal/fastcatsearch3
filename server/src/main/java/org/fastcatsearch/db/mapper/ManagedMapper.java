package org.fastcatsearch.db.mapper;

public interface ManagedMapper {
	public void createTable() throws Exception;
	
	public void createIndex() throws Exception;
	
	public void validateTable() throws Exception;
	
	public void dropTable() throws Exception;
}
