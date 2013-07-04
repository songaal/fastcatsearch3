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

package org.fastcatsearch.ir.group;

import java.io.IOException;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.query.AllDocumentOperatedClause;
import org.fastcatsearch.ir.query.Clause;
import org.fastcatsearch.ir.query.ClauseException;
import org.fastcatsearch.ir.query.Filters;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.HitFilter;
import org.fastcatsearch.ir.query.OperatedClause;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.FieldIndexesReader;
import org.fastcatsearch.ir.search.GroupIndexReader;
import org.fastcatsearch.ir.search.GroupIndexesReader;
import org.fastcatsearch.ir.search.SearchIndexesReader;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GroupHit {
	private static Logger logger = LoggerFactory.getLogger(GroupHit.class);
	private final static int BULK_SIZE = 100;
	private GroupDataGenerator groupGenerator;
	private HitFilter hitFilter;
	private int searchTotalCount;
	
	private GroupData groupData;
	
	public GroupHit(Query q, Schema schema, int docCount, SearchIndexesReader searchFieldReader, FieldIndexesReader fieldIndexesReader
			, GroupIndexesReader groupIndexesReader, BitSet localDeleteSet) throws ClauseException, IOException, IRException{
		
		Groups groups = q.getGroups();
		if(groups == null){
			return;
		}
		
		//Search
		Clause clause = q.getClause();
		OperatedClause operatedClause = null;
		
		long st = System.currentTimeMillis();
		
		if(clause == null){
			operatedClause = new AllDocumentOperatedClause(docCount);
		}else{
			operatedClause = clause.getOperatedClause(docCount, searchFieldReader);
		}
		logger.debug("getOperatedClause time = "+(System.currentTimeMillis() - st));
		
		//filter
		Filters filters = q.getFilters();
		if(filters != null){
			try{
				hitFilter = filters.getHitFilter(schema.fieldSettingMap(), fieldIndexesReader, BULK_SIZE);
			}catch(FilterException e){
				logger.error("패턴의 길이가 필드길이보다 커서 필터링을 수행할수 없습니다.", e);
			}
		}

		groupGenerator = groups.getGroupDataGenerator(schema, groupIndexesReader, fieldIndexesReader);
		
		RankInfo[] rankInfoList = new RankInfo[BULK_SIZE];
		boolean exausted = false;
		
		while(!exausted){
			int nread = 0;
			
			//search 
			for (nread = 0; nread < BULK_SIZE; nread++) {
				RankInfo rankInfo = new RankInfo();
				if(operatedClause.next(rankInfo)){
					rankInfoList[nread] = rankInfo;
				}else {
					exausted = true;
					break;
				}
			}
			
			if(filters != null && hitFilter != null){
				nread = hitFilter.filtering(rankInfoList, nread);
			}
			
			if(!exausted && nread == 0){
				continue;
			}
			
			//check delete documents
			int count = 0;
			for (int i = 0; i < nread; i++) {
				RankInfo rankInfo = rankInfoList[i];
				//Check deleted list
				if(!localDeleteSet.isSet(rankInfo.docNo())){
					rankInfoList[count] = rankInfo;
					count++;
//					logger.debug("ok docNo = "+rankInfo.docNo()+", global="+ (baseDocNo+rankInfo.docNo()));
				}else{
//					logger.debug("deleted docNo = "+rankInfo.docNo()+", global="+ (baseDocNo+rankInfo.docNo()));
				}
			}
			nread = count;
			
			//group 
			groupGenerator.insert(rankInfoList, nread);
			searchTotalCount += nread;
		}
		
		
		//
		//
		//FIXME 여기서는 아직까지 GroupData를 만들필요는 없다. 
		//한 서버내의 동일 컬렉션 내에서는 그룹번호로 식별가능하므로, 키를 읽어오는것은 머징할때 수행하도록 하면 성능향상에도움이 된다.
		//
		//
		groupData = groupGenerator.generate();
		
	}
	
	public GroupData groupData(){
		return groupData;
	}
	public int searchTotalCount(){
		return searchTotalCount;
	}
	
}
