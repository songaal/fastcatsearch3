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

public class CustomDictionaryDAO extends DictionaryDAO {

	private String[] columns;

	public CustomDictionaryDAO(String tableName, List<ColumnSetting> columnSettingList, InternalDBModule internalDBModule) {
		super(tableName, columnSettingList, internalDBModule);

		columns = new String[columnSettingList.size()];
		
		for (int i = 0; i < columnSettingList.size(); i++) {
			ColumnSetting columnSetting = columnSettingList.get(i);
			columns[i] = columnSetting.getName();
		}
	}

	public int putEntry(Object[] values) throws Exception {
		if (columns.length != values.length) {
			throw new RuntimeException("Custom dictionary column size different from values length. column = " + columnSettingList.size()
					+ " : values = " + values.length);
		}

		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			return putEntry(columns, values);
		} finally {
			mapperContext.closeSession();
		}
	}

	public int updateEntry(int id, Object[] values) throws Exception {
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			return updateEntry(id, columns, values);
		} finally {
			mapperContext.closeSession();
		}
	}

}
