package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.GroupAuthorityVO;

public interface GroupAuthorityMapper extends ManagedMapper {
	
	public GroupAuthorityVO getEntry(@Param("groupId")int groupId, @Param("authorityCode")String authorityCode) throws Exception;
	
	public List<GroupAuthorityVO> getEntryList(@Param("groupId")int groupId) throws Exception;
	
	public List<GroupAuthorityVO> getAllEntryList() throws Exception;
	
	public List<GroupAuthorityVO> getEntryListByAuthorityCode(@Param("authorityCode")String authorityCode) throws Exception;
	
	public int getCount(@Param("groupId")int groupId);
	
	public int getCountByAuthorityCode(@Param("authorityCode")String authorityCode);

	public void putEntry(GroupAuthorityVO vo) throws Exception;
	
	public void updateEntry(GroupAuthorityVO vo) throws Exception;
	
	public void deleteEntry(@Param("groupId")int groupId);
}