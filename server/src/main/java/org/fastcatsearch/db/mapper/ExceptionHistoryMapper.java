package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.ExceptionVO;

public interface ExceptionHistoryMapper {
	
	public void createTable() throws Exception;
	
	public void createIndex() throws Exception;
	
	public void validateTable() throws Exception;
	
	public void dropTable() throws Exception;

	public ExceptionVO getEntry(@Param("id") int id) throws Exception;
	
	public List<ExceptionVO> getEntryList(@Param("start") int start, @Param("end") int end) throws Exception;
	
	public int getCount() throws Exception;
	
	public void putEntry(ExceptionVO vo) throws Exception;
	
}
