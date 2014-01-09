package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.IndexingStatusVO;
import org.fastcatsearch.ir.common.IndexingType;

/*
 * IndexingHistoryMapper 와 테이블명만 다르고 동일하다.
 * */
public interface IndexingResultMapper extends ManagedMapper {
	
	public static enum ResultStatus { SUCCESS, FAIL, CANCEL, RUNNING, STOP };
	
	public IndexingStatusVO getEntry(@Param("id") int id) throws Exception;
	
	public List<IndexingStatusVO> getEntryList(@Param("collectionId") String collectionId) throws Exception;
	
	public int getCount(@Param("collectionId") String collectionId) throws Exception;
	
	public void putEntry(IndexingStatusVO vo) throws Exception;
	
	public void deleteEntry(@Param("collectionId") String collectionId, @Param("type") IndexingType type);

	public List<IndexingStatusVO> getRecentEntryList(@Param("collectionId")String collectionId, @Param("num")int num);
	
}
