package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.NotificationConfigVO;

public interface NotificationConfigMapper extends ManagedMapper {
	
	public NotificationConfigVO getEntry(@Param("code") String code) throws Exception;
	
	public List<NotificationConfigVO> getEntryList() throws Exception;
	
	public void putEntry (NotificationConfigVO vo) throws Exception;
	
	public void updateEntry (NotificationConfigVO vo) throws Exception;
	
	public void deleteEntry (@Param("code") String code) throws Exception;
}
