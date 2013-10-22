package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.TaskHistoryVO;

public interface TaskHistoryMapper extends ManagedMapper {
	
	public TaskHistoryVO getEntry(@Param("id") int id) throws Exception;
	
	public List<TaskHistoryVO> getEntryList(@Param("start") int start, @Param("end") int end) throws Exception;
	
	public int getCount() throws Exception;
	
	public void putEntry(TaskHistoryVO vo) throws Exception;
	
}
