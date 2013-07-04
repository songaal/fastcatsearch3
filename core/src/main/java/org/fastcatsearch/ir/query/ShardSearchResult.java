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

import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.search.HitElement;

public class ShardSearchResult {

	private String collectionId;
	private int shardId;
	
	private int count;
	private int totalCount;
	private HitElement[] rows;
	private GroupData groupData;
	
	public ShardSearchResult(){}
	public ShardSearchResult(HitElement[] rows, int count, int totalCount, GroupData groupData){
		this(null, -1, rows, count, totalCount, groupData);
	}
	public ShardSearchResult(int shardId, HitElement[] rows, int count, int totalCount, GroupData groupData){
		this(null, shardId, rows, count, totalCount, groupData);
	}
	public ShardSearchResult(String collectionId, int shardId, HitElement[] rows, int count, int totalCount, GroupData groupData){
		this.collectionId = collectionId;
		this.shardId = shardId;
		this.rows = rows;
		this.count = count;
		this.totalCount = totalCount;
		this.groupData = groupData;
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
	
	public void setGroupData(GroupData groupData){
		this.groupData = groupData;
	}
	
	public GroupData getGroupData(){
		return groupData;
	}
	
	public FixedHitReader getFixedHitReader(){
		return new FixedHitReader(collectionId, shardId, rows, 0, count);
	}
	
	public String toString(){
		if(groupData != null){
			return "[Result]collectionId="+collectionId+", shardId="+shardId+", count = "+count+", totalCount = "+totalCount+", groupResult.length = "+groupData.groupSize();
		}else{
			return "[Result]collectionId="+collectionId+", shardId="+shardId+", count = "+count+", totalCount = "+totalCount;
		}
	}
	
}
