package org.fastcatsearch.db.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface DictionaryMapper {
	
	public void creatTable(@Param("table") String table, @Param("size") int size, @Param("fieldList") String... fieldList) throws Exception;
	
	public void validateTable(@Param("table") String table, @Param("fieldList") String... fieldList) throws Exception;
	
	public void dropTable(@Param("table") String table) throws Exception;

	public Map<String, Object> getEntry(@Param("table") String table, @Param("id") int id) throws Exception;
	
	public List<Map<String, Object>> getEntryList(@Param("table") String table, @Param("start") int start, @Param("end") int end, @Param("search") String search, @Param("fieldList") String... fieldList) throws Exception;
	
	public int getCount(@Param("table") String table, @Param("search") String search, @Param("fieldList") String... fieldList) throws Exception;
	
	public void putEntry(@Param("table") String table,@Param("keyword") String keyword, @Param("keyValueList") KeyValue... keyValueList) throws Exception;
	
	public void updateEntry(@Param("table") String table, @Param("id") int id, @Param("keyword") String keyword, @Param("keyValueList") KeyValue... keyValueList) throws Exception;
	
	public void deleteEntry(@Param("table") String table, @Param("id") int id) throws Exception;
	
	public static class KeyValue {
		public String key;
		public String value;
		
		public KeyValue(String key, String value){
			this.key = key;
			this.value = value;
		}
	}
}
