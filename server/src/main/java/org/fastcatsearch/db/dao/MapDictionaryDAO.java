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

import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.DictionaryMapper;
import org.fastcatsearch.db.mapper.DictionaryMapper.KeyValue;

public class MapDictionaryDAO extends AbstractDictionaryDAO {

	public MapDictionaryDAO(String dictionaryId, InternalDBModule internalDBModule) {
		super(dictionaryId, new String[] { "value" }, internalDBModule);
	}

	public void putEntry(String keyword, String value) throws Exception {
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			mapperContext.getMapper().putEntry(tableName, keyword, new KeyValue(valueFieldList[0], value));
		} finally {
			mapperContext.closeSession();
		}

	}

	public void updateEntry(int id, String keyword, String value) throws Exception {
		MapperSession<DictionaryMapper> mapperContext = openMapper();
		try {
			mapperContext.getMapper().updateEntry(tableName, id, keyword, new KeyValue(valueFieldList[0], value));
		} finally {
			mapperContext.closeSession();
		}
	}

}
