/*
 * Copyright (c) 2013 Websquared, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     swsong - initial API and implementation
 */

package org.fastcatsearch.db.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.DictionaryMapper;
import org.fastcatsearch.db.mapper.DictionaryMapper.KeyValue;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryDAO {

	protected static final Logger logger = LoggerFactory.getLogger(DictionaryDAO.class);

	protected String tableName;
	
	protected List<ColumnSetting> columnSettingList;
	protected String[] searchColumns;
	private InternalDBModule internalDBModule;

	public DictionaryDAO(String tableName, List<ColumnSetting> columnSettingList, InternalDBModule internalDBModule) {
		this.tableName = tableName;
		this.columnSettingList = columnSettingList;
		this.internalDBModule = internalDBModule;
		
		searchColumns = new String[columnSettingList.size()];
		List<String> tmp = new ArrayList<String>();
		for (int i = 0; i < columnSettingList.size(); i++) {
			ColumnSetting columnSetting = columnSettingList.get(i);
			if(columnSetting.isSearchable()){
				tmp.add(columnSetting.getName());
			}
		}
		searchColumns = tmp.toArray(new String[0]);
	}

	public List<ColumnSetting> columnSettingList(){
		return columnSettingList;
	}
	
	protected MapperSession<DictionaryMapper> openMapper() {
		SqlSession session = internalDBModule.openSession();
		if (session != null) {
			return new MapperSession<DictionaryMapper>(session, session.getMapper(DictionaryMapper.class));
		}
		return null;
	}

	public boolean creatTable() {
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			mapperContext.getMapper().createTable(tableName, columnSettingList);
			mapperContext.commit();
			for(ColumnSetting columnSetting : columnSettingList){
				if(columnSetting.isIndex() || columnSetting.isSearchable()){
					String columnName = columnSetting.getName();
					mapperContext.getMapper().createIndex(tableName, columnName);
					mapperContext.commit();
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("create table error", e.getMessage());
			return false;
		} finally {
			logger.debug("create dictionary table > {}", tableName);
			mapperContext.closeSession();
		}
	}

	public boolean validateTable() {
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			mapperContext.getMapper().validateTable(tableName, columnSettingList);
			return true;
		} catch (Exception e) {
			logger.debug("validate table error", e.getMessage());
			return false;
		} finally {
			mapperContext.closeSession();
		}
	}

	public boolean dropTable() {
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			mapperContext.getMapper().dropTable(tableName);
			return true;
		} catch (Exception e) {
			logger.debug("dropTable table error", e.getMessage());
			return false;
		} finally {
			mapperContext.closeSession();
		}
	}

	public int deleteEntry(int id) throws Exception {
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			return mapperContext.getMapper().deleteEntry(tableName, id);
		} finally {
			mapperContext.closeSession();
		}
	}

	public Map<String, Object> getEntry(int id) throws Exception {
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			return mapperContext.getMapper().getEntry(tableName, id);
		} finally {
			mapperContext.closeSession();
		}
	}
	
	public List<Map<String, Object>> getEntryList(int start, int end, String search) throws Exception {
		return getEntryList(start, end, search, searchColumns);
	}
	
	public List<Map<String, Object>> getEntryList(int start, int end, String search, String[] columns) throws Exception {
		if(columns == null){
			columns = searchColumns;
		}
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			return mapperContext.getMapper().getEntryList(tableName, start, end, search, columns);
		} finally {
			mapperContext.closeSession();
		}
	}

	public int getCount(String search) throws Exception {
		return getCount(search, searchColumns);
	}
	
	public int getCount(String search, String[] columns) throws Exception {
		if(columns == null){
			columns = searchColumns;
		}
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			return mapperContext.getMapper().getCount(tableName, search, columns);
		} finally {
			mapperContext.closeSession();
		}
	}

	public int updateEntry(int id, String[] columns, Object[] values) throws Exception {
		if (columns.length != values.length) {
			throw new IllegalArgumentException("update value length is different from columns.length. " + columns.length + " != "
					+ values.length);
		}
		
		KeyValue[] keyValueList = new KeyValue[columns.length];
		for (int i = 0; i < keyValueList.length; i++) {
			keyValueList[i] = new KeyValue(columns[i], values[i]);
		}
		
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			return mapperContext.getMapper().updateEntry(tableName, id, keyValueList);
		} finally {
			mapperContext.closeSession();
		}

	}
	
	public int putEntry(String[] columns, Object[] values) throws Exception {
		if (columns.length != values.length) {
			throw new IllegalArgumentException("put values length is different from columns.length. " + columns.length + " != "
					+ values.length);
		}

		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			return mapperContext.getMapper().putEntry(tableName, columns, values);
		} finally {
			mapperContext.closeSession();
		}
	}
}
