package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.GroupAccountVO;

public interface GroupAccountMapper extends ManagedMapper {
	
	public GroupAccountVO getEntry(@Param("id") int id) throws Exception;
	
	public List<GroupAccountVO> getEntryList() throws Exception;
	
	public int getCount() throws Exception;
	
	public int getMaxId() throws Exception;
	
	public void putEntry(GroupAccountVO vo) throws Exception;
	
	public void updateEntry(GroupAccountVO vo) throws Exception;
	
	public void deleteEntry(@Param("groupId")int groupId);
}
