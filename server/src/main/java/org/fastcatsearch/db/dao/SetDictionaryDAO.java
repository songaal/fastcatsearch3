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

import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.DictionaryMapper;
import org.fastcatsearch.plugin.analysis.AnalysisPluginSetting.ColumnSetting;

public class SetDictionaryDAO extends DictionaryDAO {

	protected String[] columns;

	public SetDictionaryDAO(String tableName, List<ColumnSetting> columnSettingList, InternalDBModule internalDBModule) {
		super(tableName, columnSettingList, internalDBModule);
		if (columnSettingList == null || columnSettingList.size() != 1) {
			throw new RuntimeException("Set dictionary column size must be 1. current = " + columnSettingList.size());
		}
		columns = new String[] { columnSettingList.get(0).getName() };
	}

	public int putEntry(Object keyword) throws Exception {
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			return putEntry(columns, new Object[] { keyword });
		} finally {
			mapperContext.closeSession();
		}
	}

	public int updateEntry(int id, Object keyword) throws Exception {
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			return updateEntry(id, columns, new Object[] { keyword });
		} finally {
			mapperContext.closeSession();
		}
	}

}
