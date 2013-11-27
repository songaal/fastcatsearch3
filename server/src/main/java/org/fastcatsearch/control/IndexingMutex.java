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

package org.fastcatsearch.control;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.fastcatsearch.job.indexing.IndexingJob;


public class IndexingMutex {

	private Map<Long, String> jobIdMap;
	private Map<String, String> jobMonitorMap;
	
	public IndexingMutex(){
		jobIdMap = new HashMap<Long, String>();
		jobMonitorMap = new HashMap<String, String>();
	}
	
	public Collection<String> getIndexingList(){
		return jobMonitorMap.values();
	}
	public synchronized void release(long jobId) {
		String collectionId = jobIdMap.remove(jobId);
		if(collectionId != null){
			unlock(collectionId);
		}
	}
	
	protected void unlock(String collectionId){
		jobMonitorMap.remove(collectionId);
	}

	public synchronized void access(long jobId, IndexingJob job) {
		String collectionId = job.getStringArgs();
		if(jobMonitorMap.get(collectionId) != null){
			return;
		}
		
		jobIdMap.put(jobId, collectionId);
		jobMonitorMap.put(collectionId, job.getClass().getName());
	}

	public synchronized boolean isLocked(IndexingJob job) {
		String collectionId = job.getStringArgs();
		if(jobMonitorMap.get(collectionId) != null){
			return true;
		}
		return false;
	}

}
