package org.fastcatsearch.db;

import java.util.Map;

import org.apache.ibatis.annotations.Param;

/*
 * 테이블명까지 받아서 범용적으로 사용.
 * */
public interface TestMapper2 {
	
	public Map<String, Object> selectWord(@Param("tableName") String tableName, @Param("id") int id);
	
}
