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

import org.apache.ibatis.session.SqlSession;
import org.fastcatsearch.db.InternalDBModule;
import org.fastcatsearch.db.InternalDBModule.MapperSession;
import org.fastcatsearch.db.mapper.DictionaryStatusMapper;
import org.fastcatsearch.db.vo.DictionaryStatusVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictionaryStatusDAO {

	protected static final Logger logger = LoggerFactory.getLogger(DictionaryStatusDAO.class);

	private InternalDBModule internalDBModule;

	public DictionaryStatusDAO(InternalDBModule internalDBModule) {
		this.internalDBModule = internalDBModule;
	}

	public MapperSession<DictionaryStatusMapper> openMapperSession() {
		SqlSession session = internalDBModule.openSession();
		if (session != null) {
			return new MapperSession<DictionaryStatusMapper>(session, session.getMapper(DictionaryStatusMapper.class));
		}
		return null;
	}
	
	public boolean creatTable() {
		MapperSession<DictionaryStatusMapper> mapperSession = openMapperSession();
		try {
			mapperSession.getMapper().createTable();
			mapperSession.commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("create table error", e.getMessage());
			return false;
		} finally {
			logger.debug("create DictionaryStatus");
			mapperSession.closeSession();
		}
	}

	public boolean validateTable() {
		MapperSession<DictionaryStatusMapper> mapperSession = openMapperSession();
		try {
			mapperSession.getMapper().validateTable();
			return true;
		} catch (Exception e) {
			logger.debug("validate table error", e.getMessage());
			return false;
		} finally {
			mapperSession.closeSession();
		}
	}

	public boolean dropTable() {
		MapperSession<DictionaryStatusMapper> mapperSession = openMapperSession();
		try {
			mapperSession.getMapper().dropTable();
			return true;
		} catch (Exception e) {
			logger.debug("dropTable table error", e.getMessage());
			return false;
		} finally {
			mapperSession.closeSession();
		}
	}

	public int deleteEntry(String dictionaryId) throws Exception {
		MapperSession<DictionaryStatusMapper> mapperSession = openMapperSession();
		try {
			return mapperSession.getMapper().deleteEntry(dictionaryId);
		} finally {
			mapperSession.closeSession();
		}
	}
	
	public DictionaryStatusVO getEntry(String dictionaryId) throws Exception {
		MapperSession<DictionaryStatusMapper> mapperSession = openMapperSession();
		try {
			return mapperSession.getMapper().getEntry(dictionaryId);
		} finally {
			mapperSession.closeSession();
		}
	}

	public int updateApplyStatus(String dictionaryId, int applyEntrySize) throws Exception {
		MapperSession<DictionaryStatusMapper> mapperSession = openMapperSession();
		try {
			return mapperSession.getMapper().updateApplyStatus(dictionaryId, applyEntrySize);
		} finally {
			mapperSession.closeSession();
		}
	}
	
	public int updateUpdateTime(String dictionaryId) throws Exception {
		MapperSession<DictionaryStatusMapper> mapperSession = openMapperSession();
		try {
			return mapperSession.getMapper().updateUpdateTime(dictionaryId);
		} finally {
			mapperSession.closeSession();
		}
	}
	
	public int putEntry(DictionaryStatusVO vo) throws Exception {
		MapperSession<DictionaryStatusMapper> mapperSession = openMapperSession();
		try {
			return mapperSession.getMapper().putEntry(vo);
		} finally {
			mapperSession.closeSession();
		}
	}
	
	public int truncate() throws Exception {
		MapperSession<DictionaryStatusMapper> mapperSession = openMapperSession();
		try {
			return mapperSession.getMapper().truncate();
		} finally {
			mapperSession.closeSession();
		}
	}
}
