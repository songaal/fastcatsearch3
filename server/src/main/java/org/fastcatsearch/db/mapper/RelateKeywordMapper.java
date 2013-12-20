package org.fastcatsearch.db.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.RelateKeywordVO;

/*
 * 연관키워드 테이블.
 * */
public interface RelateKeywordMapper extends ManagedMapper {
	
	public RelateKeywordVO getEntry(@Param("category") String category, @Param("keyword") String keyword) throws Exception;
	
	public List<RelateKeywordVO> getEntryList(@Param("category") String category) throws Exception;
	
	public List<RelateKeywordVO> getEntryListByWhereCondition(@Param("category") String category, @Param("whereCondition") String whereCondition ,@Param("start")int start, @Param("end")int end) throws Exception; 
	
	public void putEntry(RelateKeywordVO vo) throws Exception;
	
	public void deleteEntry(@Param("id") int id);
	
	public int getCount(@Param("category") String category) throws Exception;
	
	public int getCountByWhereCondition(@Param("category") String category, @Param("whereCondition") String whereCondition) throws Exception;
	
}
