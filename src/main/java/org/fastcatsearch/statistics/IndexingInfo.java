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

package org.fastcatsearch.statistics;

public class IndexingInfo {
	public String collectionName;
	public int fullDoc;
	public int fullInsert;
	public int fullUpdate;
	public int fullDelete;
	public int incDoc;
	public int incInsert;
	public int incUpdate;
	public int incDelete;
	public int totalDoc;
	public long updateTime;
	
	public IndexingInfo(String collectionName){
		this.collectionName = collectionName;
	}
	
	public void reset(){
		fullDoc = 0;
		fullInsert = 0;
		fullUpdate = 0;
		fullDelete = 0;
		incDoc = 0;
		incInsert = 0;
		incUpdate = 0;
		incDelete = 0;
		totalDoc = 0;
	}
}
