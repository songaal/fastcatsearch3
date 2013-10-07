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

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.db.InternalDBModule.SessionAndMapper;
import org.fastcatsearch.db.mapper.DictionaryMapper;
import org.fastcatsearch.db.mapper.DictionaryMapper.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractDictionaryDAO {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractDictionaryDAO.class);

	protected String tableName;

	protected String[] valueFieldList;

	private InternalDBModule internalDBModule;

	public AbstractDictionaryDAO(String tableName, String[] valueFieldList, InternalDBModule internalDBModule) {
		this.tableName = tableName;
		this.valueFieldList = valueFieldList;
		this.internalDBModule = internalDBModule;
	}

	protected SessionAndMapper<DictionaryMapper> openMapper() {
		SqlSession session = internalDBModule.openSession();
		if (session != null) {
			return new SessionAndMapper<DictionaryMapper>(session, DictionaryMapper.class);
		}
		return null;
	}

	public boolean creatTable(int fieldLength) {
		SessionAndMapper<DictionaryMapper> mapperContext = openMapper();
		try {
			mapperContext.getMapper().creatTable(tableName, fieldLength, valueFieldList);
			return true;
		} catch (Exception e) {
			logger.debug("create table error", e.getMessage());
			return false;
		} finally {
			mapperContext.closeSession();
		}
	}

	public boolean validateTable() {
		SessionAndMapper<DictionaryMapper> mapperContext = openMapper();
		try {
			mapperContext.getMapper().validateTable(tableName, valueFieldList);
			return true;
		} catch (Exception e) {
			logger.debug("validate table error", e.getMessage());
			return false;
		} finally {
			mapperContext.closeSession();
		}
	}

	public boolean dropTable() {
		SessionAndMapper<DictionaryMapper> mapperContext = openMapper();
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

	public void deleteEntry(int id) throws Exception {
		SessionAndMapper<DictionaryMapper> mapperContext = openMapper();
		try {
			mapperContext.getMapper().deleteEntry(tableName, id);
		} finally {
			mapperContext.closeSession();
		}
	}

	public Map<String, Object> getEntry(int id) throws Exception {
		SessionAndMapper<DictionaryMapper> mapperContext = openMapper();
		try {
			return mapperContext.getMapper().getEntry(tableName, id);
		} finally {
			mapperContext.closeSession();
		}
	}

	public List<Map<String, Object>> getEntryList(int start, int end, String search) throws Exception {
		SessionAndMapper<DictionaryMapper> mapperContext = openMapper();
		try {
			return mapperContext.getMapper().getEntryList(tableName, start, end, search, valueFieldList);
		} finally {
			mapperContext.closeSession();
		}
	}

	public int getCount(String search) throws Exception {
		SessionAndMapper<DictionaryMapper> mapperContext = openMapper();
		try {
			return mapperContext.getMapper().getCount(tableName, search, valueFieldList);
		} finally {
			mapperContext.closeSession();
		}
	}

	public void updateEntry(int id, String keyword, String... values) throws Exception {
		SessionAndMapper<DictionaryMapper> mapperContext = openMapper();
		try {
			if (values.length != valueFieldList.length) {
				throw new IllegalArgumentException("update value length is different from valueFieldList.length. " + values.length + " != "
						+ valueFieldList.length);
			}
			KeyValue[] list = new KeyValue[values.length];
			for (int i = 0; i < list.length; i++) {
				list[i] = new KeyValue(valueFieldList[i], values[i]);
			}
			mapperContext.getMapper().updateEntry(tableName, id, keyword, list);
		} finally {
			mapperContext.closeSession();
		}

	}
	
	public void putEntry(String keyword, String... values) throws Exception {
		if (values.length != valueFieldList.length) {
			throw new IllegalArgumentException("input value length is different from valueFieldList.length. " + values.length + " != "
					+ valueFieldList.length);
		}

		SessionAndMapper<DictionaryMapper> mapperContext = openMapper();
		try {
			KeyValue[] list = new KeyValue[values.length];
			for (int i = 0; i < list.length; i++) {
				list[i] = new KeyValue(valueFieldList[i], values[i]);
			}
			mapperContext.getMapper().putEntry(tableName, keyword, list);
		} finally {
			mapperContext.closeSession();
		}
	}
}
