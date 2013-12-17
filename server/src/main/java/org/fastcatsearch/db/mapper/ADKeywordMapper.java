package org.fastcatsearch.db.mapper;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.db.vo.ADKeywordVO;

/*
 * 광고키워드 테이블.
 * */
public interface ADKeywordMapper extends ManagedMapper {
	
	public ADKeywordVO getEntryList(@Param("keyword") String keyword) throws Exception;
	
	public void putEntry(ADKeywordVO vo) throws Exception;
	
	public void deleteEntry(@Param("id") int id);
	
}
