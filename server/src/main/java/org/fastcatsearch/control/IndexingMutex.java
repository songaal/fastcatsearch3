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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fastcatsearch.job.FullIndexJob;
import org.fastcatsearch.job.IncIndexJob;
import org.fastcatsearch.job.Job;


public class IndexingMutex {

	private Map<Long, String> jobIdMap;
	private Set<String> monitors;
	private Map<String, String> jobTypeMap;
	
	public IndexingMutex(){
		jobIdMap = new HashMap<Long, String>();
		monitors = new HashSet<String>();
		jobTypeMap = new HashMap<String, String>();
	}
	
	public Collection<String> getIndexingList(){
		return jobTypeMap.values();
	}
	public synchronized void release(long jobId) {
		String collection = jobIdMap.remove(jobId);
		if(collection != null){
			monitors.remove(collection);
			jobTypeMap.remove(collection);
		}
	}

	public synchronized void access(long myJobId, Job job) {
		if(job instanceof FullIndexJob || job instanceof IncIndexJob){// || job instanceof RebuildIndexJob){
			String collection = job.getStringArgs(0);
			if(monitors.contains(collection)){
				return;
			}
		}else{
			//not an indexing job
			return;
		}
		
		String collection = job.getStringArgs(0);
		String type = null;
		if(job instanceof FullIndexJob){
			type = "full";
		}else if(job instanceof IncIndexJob){
			type = "add";
		}
//		else if(job instanceof RebuildIndexJob) {
//			type = "rebuild";
//		}
		
		monitors.add(collection);
		jobIdMap.put(myJobId, collection);
		jobTypeMap.put(collection, collection+"."+type);
	}

	public synchronized boolean isLocked(Job job) {
		if(job instanceof FullIndexJob || job instanceof IncIndexJob){// || job instanceof RebuildIndexJob){
			String collection = job.getStringArgs(0);
			if(monitors.contains(collection)){
				return true;
			}
		}		
		return false;
	}

}
