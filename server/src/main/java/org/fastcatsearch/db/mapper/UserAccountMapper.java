package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.UserAccountVO;

public interface UserAccountMapper extends ManagedMapper {
	
	public UserAccountVO getEntry(@Param("id") int id) throws Exception;
	
	public UserAccountVO getEntryByUserId(@Param("userId") String userId) throws Exception;
	
	public UserAccountVO getEntryByUserIdAndPassword(@Param("userId")String userId, @Param("password")String password);
	
	public List<UserAccountVO> getEntryList() throws Exception;
	
	public int getCount() throws Exception;
	
	public void putEntry (UserAccountVO vo) throws Exception;

}
