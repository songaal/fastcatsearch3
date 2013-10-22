package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.ExceptionVO;

public interface ExceptionHistoryMapper extends ManagedMapper {

	public ExceptionVO getEntry(@Param("id") int id) throws Exception;
	
	public List<ExceptionVO> getEntryList(@Param("start") int start, @Param("end") int end) throws Exception;
	
	public int getCount() throws Exception;
	
	public void putEntry(ExceptionVO vo) throws Exception;
	
}
