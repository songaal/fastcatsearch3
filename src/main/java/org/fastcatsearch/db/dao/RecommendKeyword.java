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


public class RecommendKeyword extends MapDictionaryDAO {

	public RecommendKeyword() {
		super("RecommendKeyword");
	}

	public List<MapDictionaryVO> exactSearch(String keyword) {
		return selectWithExactKeyword(keyword);
	}
}
