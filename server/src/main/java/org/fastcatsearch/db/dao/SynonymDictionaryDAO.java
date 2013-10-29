package org.fastcatsearch.db.dao;

import java.util.List;

import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.DictionaryMapper;
import org.fastcatsearch.db.mapper.DictionaryMapper.KeyValue;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;

public class SynonymDictionaryDAO extends DictionaryDAO {

	private String nullableUniqueColumnName; //key성 컬럼이므로 하나만 존재.
	
	public SynonymDictionaryDAO(String tableName, List<ColumnSetting> columnSettingList, InternalDBModule internalDBModule) {
		super(tableName, columnSettingList, internalDBModule);
		for (int i = 0; i < columnSettingList.size(); i++) {
			ColumnSetting columnSetting = columnSettingList.get(i);
			if(columnSetting.isNullableUnique()){
				nullableUniqueColumnName = columnSetting.getName();
			}
		}
	}

	@Override
	public int putEntry(String[] columns, Object[] values) throws Exception {
		if (columns.length != values.length) {
			throw new IllegalArgumentException("put values length is different from columns.length. " + columns.length + " != "
					+ values.length);
		}

		boolean isExists = false;
		if(nullableUniqueColumnName != null){
			for(int i = 0; i < columns.length; i++){
				String column = columns[i];
				if(nullableUniqueColumnName.equalsIgnoreCase(column)){
					String value = values[i].toString();
					//값이 존재할때에만 unique체크를 한다.
					if(value == null || value.length() == 0){
						break;
					}
					MapperSession<DictionaryMapper> mapperContext = openMapperSession();
					try {
						int recordCount = mapperContext.getMapper().hasEntry(tableName, value, column);
						isExists = (recordCount > 0);
					} finally {
						mapperContext.closeSession();
					}
					
					break;
				}
			}
		
		}
		if(isExists){
			//존재하면 입력하지 않는다.
			return 0;
		}
		
		MapperSession<DictionaryMapper> mapperContext = openMapperSession();
		try {
			return mapperContext.getMapper().putEntry(tableName, columns, values);
		} finally {
			mapperContext.closeSession();
		}
	}
	
	@Override
	public int updateEntry(Object id, String[] columns, Object[] values) throws Exception {
		if (columns.length != values.length) {
			throw new IllegalArgumentException("update value length is different from columns.length. " + columns.length + " != "
					+ values.length);
		}
		
		KeyValue[] keyValueList = new KeyValue[columns.length];
		for (int i = 0; i < keyValueList.length; i++) {
			keyValueList[i] = new KeyValue(columns[i], values[i]);
		}
		
		MapperSession<DictionaryMapper> mapperContext = openMapperSession();
		try {
			int count = mapperContext.getMapper().updateEntry(tableName, id, keyValueList);
			
			//업데이트된것이 2개이상이면 중복이 있으므로 롤백한다.
			if(count > 1){
				mapperContext.rollback();
				return 0;
			}
			
			return count;
		} finally {
			mapperContext.closeSession();
		}

	}
	
}
