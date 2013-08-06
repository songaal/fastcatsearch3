/*
 * Copyright 2013 Websquared, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fastcatsearch.ir.query;

import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.search.DocIdList;
import org.fastcatsearch.ir.search.HitElement;

public class InternalSearchResult {

	private String collectionId;
	private int shardId;
	
	private int count;
	private int totalCount;
	private HitElement[] rows;
	private GroupsData groupData;
	private HighlightInfo highlightInfo;
	
	public InternalSearchResult(HitElement[] rows, int count, int totalCount, GroupsData groupData){
		this(null, -1, rows, count, totalCount, groupData, null);
	}
	public InternalSearchResult(int shardId, HitElement[] rows, int count, int totalCount, GroupsData groupData){
		this(null, shardId, rows, count, totalCount, groupData, null);
	}
	public InternalSearchResult(String collectionId, int shardId, HitElement[] rows, int count, int totalCount, GroupsData groupData, HighlightInfo highlightInfo){
		this.collectionId = collectionId;
		this.shardId = shardId;
		this.rows = rows;
		this.count = count;
		this.totalCount = totalCount;
		this.groupData = groupData;
		this.highlightInfo = highlightInfo;
	}
	
	public String collectionId(){
		return collectionId;
	}
	
	public int shardId(){
		return shardId;
	}
	
	public int getTotalCount(){
		return totalCount;
	}
	
	public int getCount(){
		return count;
	}
	
	public HitElement[] getHitElementList(){
		return rows;
	}
	
	public DocIdList getDocIdList(){
		DocIdList docIdList = new DocIdList(count);
		for (int i = 0; i < count; i++) {
			HitElement el = rows[i];
			docIdList.add(el.segmentSequence(), el.docNo());
		}
		return docIdList;
	}
	public void setGroupData(GroupsData groupData){
		this.groupData = groupData;
	}
	
	public GroupsData getGroupsData(){
		return groupData;
	}
	
	public HighlightInfo getHighlightInfo(){
		return highlightInfo;
	}
	public FixedHitReader getFixedHitReader(){
		return new FixedHitReader(collectionId, shardId, rows, 0, count);
	}
	
	public String toString(){
		if(groupData != null){
			return "[Result]collectionId="+collectionId+", shardId="+shardId+", count = "+count+", totalCount = "+totalCount+", groupResult.length = "+groupData.groupSize()+", highlightInfo = "+highlightInfo;
		}else{
			return "[Result]collectionId="+collectionId+", shardId="+shardId+", count = "+count+", totalCount = "+totalCount+", highlightInfo = "+highlightInfo;
		}
	}
	
}
