package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.NotificationVO;

public interface NotificationHistoryMapper extends ManagedMapper {
	
	public NotificationVO getEntry(@Param("id") int id) throws Exception;
	
	public List<NotificationVO> getEntryList(@Param("start") int start, @Param("end") int end) throws Exception;
	
	public int getCount() throws Exception;
	
	public void putEntry(NotificationVO vo) throws Exception;
	
}
