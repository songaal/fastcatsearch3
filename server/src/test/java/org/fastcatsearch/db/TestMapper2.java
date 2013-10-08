package org.fastcatsearch.db;

import org.apache.ibatis.annotations.Param;

/*
 * 테이블명까지 받아서 범용적으로 사용.
 * */
public interface TestMapper2 {
	
	public void createTable();
	
	public void dropTable();
	
	public TestVO selectWord(@Param("id") int id);
	
	public void insertWord(TestVO vo);

	public void createIndex();
}
