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

import java.util.List;

import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.search.DocIdList;
import org.fastcatsearch.ir.search.Explanation;
import org.fastcatsearch.ir.search.HitElement;

public class InternalSearchResult {

	private String collectionId;

	private int count;
	private int totalCount;
	private HitElement[] rows;
	private GroupsData groupData;
	private HighlightInfo highlightInfo;
	private List<Explanation> explanations;

	private String nodeId;
	
	public InternalSearchResult(HitElement[] rows, int count, int totalCount, GroupsData groupData, List<Explanation> explanations) {
		this(null, rows, count, totalCount, groupData, null, explanations);
	}

	public InternalSearchResult(String collectionId, HitElement[] rows, int count, int totalCount, GroupsData groupData, HighlightInfo highlightInfo, List<Explanation> explanations) {
		this.collectionId = collectionId;
		this.rows = rows;
		this.count = count;
		this.totalCount = totalCount;
		this.groupData = groupData;
		this.highlightInfo = highlightInfo;
		this.explanations = explanations;
	}

	public String collectionId() {
		return collectionId;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public int getCount() {
		return count;
	}

	public HitElement[] getHitElementList() {
		return rows;
	}

	public DocIdList getDocIdList() {
		DocIdList docIdList = new DocIdList(count);
		for (int i = 0; i < count; i++) {
			HitElement el = rows[i];
			docIdList.add(el.segmentId(), el.docNo());
		}
		return docIdList;
	}

	public void setGroupData(GroupsData groupData) {
		this.groupData = groupData;
	}

	public GroupsData getGroupsData() {
		return groupData;
	}

	public HighlightInfo getHighlightInfo() {
		return highlightInfo;
	}

	public FixedHitReader getFixedHitReader() {
		return new FixedHitReader(collectionId, rows, 0, count);
	}

	public List<Explanation> getExplanations() {
		return explanations;
	}
	
	public void setExplanations(List<Explanation> explanations) {
		this.explanations = explanations;
	}
	
	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	public String toString() {
		if (groupData != null) {
			return "[Result]collectionId=" + collectionId + ", count = " + count + ", totalCount = " + totalCount + ", groupResult.length = " + groupData.groupSize()
					+ ", highlightInfo = " + highlightInfo + ", explanations = " + explanations;
		} else {
			return "[Result]collectionId=" + collectionId + ", count = " + count + ", totalCount = " + totalCount + ", highlightInfo = " + highlightInfo + ", explanations = " + explanations;
		}
	}

}
