package org.fastcatsearch.ir.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fastcatsearch.ir.group.GroupsData;
import org.fastcatsearch.ir.group.GroupDataMerger;
import org.fastcatsearch.ir.io.FixedHitQueue;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.io.FixedMinHeap;
import org.fastcatsearch.ir.query.Bundle;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.InternalSearchResult;
import org.fastcatsearch.ir.query.Sorts;
import org.fastcatsearch.ir.settings.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchResultAggregator {
	protected static Logger logger = LoggerFactory.getLogger(SearchResultAggregator.class);
	
	private Schema schema;
	private Query q;
	
	public SearchResultAggregator(Query q, Schema schema){
		this.q = q;
		this.schema = schema;
	}
	
	public InternalSearchResult aggregate(List<InternalSearchResult> resultList) {
		
		int mergeSize = resultList.size();
		
		if(mergeSize == 0){
			//에러상황.
			return null;
		}else if(mergeSize == 1){
			//머징할것이 없는 경우 그대로 리턴.
			return resultList.get(0);
		} else {
			Groups groups = q.getGroups();
			Sorts sorts = q.getSorts();
			Metadata meta = q.getMeta();
			int start = meta.start();
			int rows = meta.rows();
			int totalCount = 0;
			int count = 0;
			
			GroupDataMerger dataMerger = null;
			
			if(groups != null){
				dataMerger = new GroupDataMerger(groups, mergeSize);
			}
			
			//정렬을 다시한번 수행해서 top N을 뽑는다.
			
			FixedMinHeap<FixedHitReader> hitMerger = null;
			
			if(sorts != null){
				try {
					hitMerger = sorts.createMerger(schema, mergeSize);
				} catch (IOException e) {
					logger.error("Merger생성중 에러발생.", e);
					return null;
				}
			}else{
				hitMerger = new FixedMinHeap<FixedHitReader>(mergeSize);
			}
			
			List<Explanation> explanationList = null;
			for (int i = 0; i < resultList.size(); i++) {
				InternalSearchResult result = resultList.get(i);
				totalCount += result.getTotalCount();
				FixedHitReader hitReader = result.getFixedHitReader();
				
				//posting data
				if(hitReader.next()){
					hitMerger.push(hitReader);
				}
				
				GroupsData groupData = result.getGroupsData();
				if(groupData != null){
					//Put GroupResult
					dataMerger.put(groupData);
				}
				
				if(result.getExplanations() != null){
					if(explanationList == null){
						explanationList = new ArrayList<Explanation>();
					}
					for(Explanation exp : result.getExplanations()){
						exp.setNodeId(result.getNodeId());
						explanationList.add(exp);
					}
				}
			}
			
			//각 shard의 결과들을 rankdata를 기준으로 재정렬한다.
			FixedHitQueue totalHit = new FixedHitQueue(rows);
			int c = 1;
			
			while(hitMerger.size() > 0){
				FixedHitReader r = hitMerger.peek();
				HitElement el = r.read();
				if(c >= start){
					totalHit.push(el);
					count++;
				}
				c++;
				
				//결과가 만들어졌으면 일찍 끝낸다.
				if(count == rows)
					break;
				
				if(!r.next()){
					//다 읽은 것은 버린다.
					hitMerger.pop();
				}
				hitMerger.heapify();
			}
			
			GroupsData groupData = null;
			if(dataMerger != null){
				groupData = dataMerger.merge();
			}

			return new InternalSearchResult(totalHit.getHitElementList(), count, totalCount, groupData, explanationList);
		}
		
	}
}
