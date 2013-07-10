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

package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.filter.FilterException;
import org.fastcatsearch.ir.group.GroupData;
import org.fastcatsearch.ir.group.GroupDataGenerator;
import org.fastcatsearch.ir.group.GroupEntryList;
import org.fastcatsearch.ir.io.BitSet;
import org.fastcatsearch.ir.io.FixedHitStack;
import org.fastcatsearch.ir.io.FixedMaxPriorityQueue;
import org.fastcatsearch.ir.query.AllDocumentOperatedClause;
import org.fastcatsearch.ir.query.Clause;
import org.fastcatsearch.ir.query.ClauseException;
import org.fastcatsearch.ir.query.Filters;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.HighlightInfo;
import org.fastcatsearch.ir.query.HitFilter;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.OperatedClause;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.query.Sorts;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HitBak {
	private static Logger logger = LoggerFactory.getLogger(Hit.class);
	private final static int BULK_SIZE = 100;
	private FixedMaxPriorityQueue<HitElement> ranker;
	private GroupDataGenerator groupGenerator;
	private HitFilter hitFilter;
	private HitFilter groupHitFilter;
	private int baseDocNo;
	private RankInfo groupClauseDocInfo = new RankInfo();
	private boolean groupClauseRemain;
	private int totalCount;
//	private ArrayList<HighlightInfo> summary;
	
	public HitBak(Query q, Schema schema, int baseDocNo, int docCount, SearchIndexesReader searchFieldReader, FieldIndexesReader fieldIndexesReader
			, GroupIndexesReader groupIndexesReader, BitSet localDeleteSet) throws ClauseException, IOException, IRException{
		
		Metadata meta = q.getMeta();
		int sortMaxSize = meta.start() + meta.rows() - 1;
		
		this.baseDocNo = baseDocNo;
		//Search
//		summary = new ArrayList<HighlightInfo>();
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

		//group
		Groups groups = q.getGroups();
		Clause groupClause = null;
		Filters groupFilters = null;
		OperatedClause groupOperatedClause = null;
		
		if(groups != null){
			groupGenerator = groups.getGroupDataGenerator(schema, groupIndexesReader, fieldIndexesReader);
			//group clause
			groupClause = q.getGroupClause();
			if(groupClause != null){
				groupOperatedClause = groupClause.getOperatedClause(docCount, searchFieldReader);
				groupClauseRemain = groupOperatedClause.next(groupClauseDocInfo);
			}
			//group filter
			groupFilters = q.getGroupFilters();
			if(groupFilters != null){
				groupHitFilter = groupFilters.getHitFilter(schema.fieldSettingMap(), fieldIndexesReader, BULK_SIZE);
			}
		}
		
		//sort
		Sorts sorts = q.getSorts();
		SortGenerator sortGenerator = null;
		if(sorts != null){
			sortGenerator = sorts.getSortGenerator(schema, fieldIndexesReader);
			//ranker에 정렬 로직이 담겨있다.
			//ranker 안에는 필드타입과 정렬옵션을 확인하여 적합한 byte[] 비교를 수행한다.
			ranker = sorts.createRanker(schema, sortMaxSize);
		}else{
			ranker = new DefaultRanker(sortMaxSize);
		}
		
		RankInfo[] rankInfoList = new RankInfo[BULK_SIZE];
		boolean exausted = false;

//		int searchTime = 0, sortTime = 0, filterTime = 0;
		while(!exausted){
			int nread = 0;
			
			//search 
			st = System.currentTimeMillis();
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
			if(groups != null){
				groupGenerator.insert(rankInfoList, nread);
				if(groupClause != null)
					nread = applyGroupClause(groupOperatedClause, rankInfoList, nread);
				
				//group filter
				if(groupFilters != null){
					nread = groupHitFilter.filtering(rankInfoList, nread);
//					logger.debug("group filter => "+nread);
				}
			}
			
			if(sorts != null){
				//if sort set 
				//sortGenerator 는 단순히 데이터를 읽어서 HitElement에 넣어주고 실제 정렬로직은 ranker에 push하면서 수행된다.
				HitElement[] e = sortGenerator.getHitElement(rankInfoList, nread);
				for (int i = 0; i < nread; i++) {
					ranker.push(e[i]);
				}
			}else{
				//if sort is not set, rankdata is null
				for (int i = 0; i < nread; i++) {
					ranker.push(new HitElement(rankInfoList[i].docNo(), rankInfoList[i].score() ));
				}
				
			}
//			sortTime += (System.currentTimeMillis() - st);
//			logger.info(nread+" sort time = "+(System.currentTimeMillis() - st));
			totalCount += nread;
		}
		
//		logger.debug("#### time = "+searchTime+", "+filterTime+", "+sortTime + "("+totalCount+")");
	}
	
	private int applyGroupClause(OperatedClause groupOperatedClause, RankInfo[] rankInfoList, int nread) {
		if(!groupClauseRemain) //if group clause set is empty, result become empty because of AND operation.  
			return 0;
		
		int n = 0;
		int p1 = 0;
		RankInfo ri = rankInfoList[p1];
		while(p1 < nread){
			int docId1 = ri.docNo();
			int docId2 = groupClauseDocInfo.docNo();
//			logger.debug("nread = "+nread+", p1 = "+p1+", "+docId1+" : "+docId2+" n ="+n);
			if(docId1 < docId2){
				p1++;
				if(p1 == nread)
					return n;
				
				ri = rankInfoList[p1];
			}else if(docId1 > docId2){
				if(!groupOperatedClause.next(groupClauseDocInfo)){
					groupClauseRemain = false;
					break;
				}
			}else{
				rankInfoList[n] = ri;
				n++;
				
				//increase doc1
				p1++;
				if(p1 == nread)
					return n;
				ri = rankInfoList[p1];
				
				//increase doc2
				if(!groupOperatedClause.next(groupClauseDocInfo)){
					groupClauseRemain = false;
					break;
				}
			}
		}
		
		return n;
	}

	public int getTotalCount(){
		return totalCount;
	}
	
	/**
	 * Top K개의 HIT들을 리턴한다.
	 * @return
	 */
	public FixedHitStack getHitList(){
		int size = ranker.size();
//		logger.debug("size="+size);
		FixedHitStack hitStack = new FixedHitStack(size);
		for (int i = 0; i < size; i++){
			HitElement el = ranker.pop();
			//local문서번호를 global문서번호로 바꿔준다.
			el.docNo(baseDocNo + el.docNo());
			hitStack.push(el);
		
		}
		return hitStack;
	}
	
	//그룹결과는 문서를 next로 다읽은 경우에 완료된다.
	public GroupData getGroupData() throws IOException{
		if(groupGenerator == null)
			return new GroupData(null, totalCount);
		
		return groupGenerator.generate();
	}
	
//	public List<HighlightInfo> getSummary(){
//		return summary;
//	}
}
