package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.IndexingStatusVO;

/*
 * IndexingResultMapper 와 테이블명만 다르고 동일하다.
 */
public interface IndexingHistoryMapper extends ManagedMapper {
	
	public IndexingStatusVO getEntry(@Param("id") int id) throws Exception;
	
	public List<IndexingStatusVO> getEntryList(@Param("collectionId") String collectionId, @Param("start") int start, @Param("end") int end) throws Exception;
	
	public int getCount(@Param("collectionId") String collectionId) throws Exception;
	
	public void putEntry(IndexingStatusVO vo) throws Exception;
	
}
