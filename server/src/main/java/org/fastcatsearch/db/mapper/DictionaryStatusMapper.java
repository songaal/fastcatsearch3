package org.fastcatsearch.db.mapper;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.DictionaryStatusVO;

public interface DictionaryStatusMapper extends ManagedMapper {

	public DictionaryStatusVO getEntry(@Param("dictionaryId") String dictionaryId) throws Exception;
	
	public int putEntry(DictionaryStatusVO vo) throws Exception;
	
	public int deleteEntry(@Param("dictionaryId") String dictionaryId) throws Exception;
	
	public int updateUpdateTime(@Param("dictionaryId") String dictionaryId) throws Exception;
	
	public int updateApplyStatus(@Param("dictionaryId") String dictionaryId, @Param("applyEntrySize") Integer applyEntrySize) throws Exception;

	public int truncate() throws Exception;
	
}
