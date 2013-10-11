package org.fastcatsearch.db;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface TestMapper {
	
	public Map<String, Object> selectWord(@Param("id") int id);
	
	public List<Map<String, Object>> selectList(@Param("start") int start, @Param("end") int end, @Param("search") String search);
	
}
