package org.fastcatsearch.db.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;

public interface DictionaryMapper {
	
	public void createTable(@Param("table") String table, @Param("columnSettings") List<ColumnSetting> columnSettings) throws Exception;
	
	public void createIndex(@Param("table") String table, @Param("column") String column) throws Exception;
	
	public void validateTable(@Param("table") String table, @Param("columnSettings") List<ColumnSetting> columnSettings) throws Exception;
	
	public void dropTable(@Param("table") String table) throws Exception;

	public Map<String, Object> getEntry(@Param("table") String table, @Param("id") Object id) throws Exception;
	
	public List<Map<String,Object>> getEntryListByWhereCondition(@Param("table") String table,@Param("whereCondition") String whereCondition) throws Exception;
	
	public List<Map<String, Object>> getEntryList(@Param("table") String table, @Param("start") int start, @Param("end") int end
			, @Param("search") String search, @Param("columns") String[] columns, @Param("sortAsc") Boolean sortAsc) throws Exception;
	
	public int hasEntry(@Param("table") String table, @Param("search") String search, @Param("column") String column) throws Exception;
	
	public int getCount(@Param("table") String table, @Param("search") String search, @Param("columns") String[] columns) throws Exception;
	
	public int putEntry(@Param("table") String table, @Param("columns") String[] columns, @Param("values") Object[] values) throws Exception;
	
	public int updateEntry(@Param("table") String table, @Param("id") Object id, @Param("keyValueList") KeyValue[] keyValueList) throws Exception;
	
	public int deleteEntry(@Param("table") String table, @Param("id") Object id) throws Exception;
	
	public int deleteEntryList(@Param("table") String table, @Param("idList") String idList) throws Exception;
	
	public int truncate(@Param("table") String table) throws Exception;
	
	public static class KeyValue {
		private String key;
		private Object value;
		
		public KeyValue(String key, Object value){
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
	}
}
