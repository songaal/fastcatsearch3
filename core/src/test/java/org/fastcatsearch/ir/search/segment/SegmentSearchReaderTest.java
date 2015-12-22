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

package org.fastcatsearch.ir.search.segment;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.DataInfo.SegmentInfo;
import org.fastcatsearch.ir.group.*;
import org.fastcatsearch.ir.io.FixedHitQueue;
import org.fastcatsearch.ir.io.FixedHitReader;
import org.fastcatsearch.ir.io.FixedHitStack;
import org.fastcatsearch.ir.io.FixedMinHeap;
import org.fastcatsearch.ir.query.Group;
import org.fastcatsearch.ir.query.Groups;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Sort;
import org.fastcatsearch.ir.query.Sorts;
import org.fastcatsearch.ir.query.Term;
import org.fastcatsearch.ir.search.Hit;
import org.fastcatsearch.ir.search.HitElement;
import org.fastcatsearch.ir.search.SegmentReader;
import org.fastcatsearch.ir.search.clause.Clause;
import org.fastcatsearch.ir.search.clause.ClauseException;
import org.fastcatsearch.ir.settings.Schema;


public class SegmentSearchReaderTest extends TestCase{
	
	public void testSingle() throws SettingException, IOException, ClauseException, IRException{
		String homePath = "testHome/";
		String collection ="test3";
		
		Schema schema = new Schema(null);//collection, true);
		File segDir = new File("");//IRSettings.getSegmentPath(collection, 0, 0));
		
		//make query
		Query q = new Query();
		Clause c = new Clause(new Term("title","티셔츠"), Clause.Operator.OR, new Term("title","반팔"));
		c = new Clause(c, Clause.Operator.OR, new Term("seller","michael"));
		q.setClause(c);
		q.setMeta(new Metadata(0,100,0,null));
		
		Sorts sorts = new Sorts();
		sorts.add(new Sort("_score_", false));
		q.setSorts(sorts);
		SegmentInfo segmentInfo = null;//new SegmentInfo(0, segDir);
		SegmentReader searcher = null;//new SegmentReader(schema, segDir, segmentInfo.getBaseDocNo(), segmentInfo.getDocCount());
//		Hit hit = searcher.search(q);
//		FixedHitStack s = hit.getHitList();
//		FixedHitReader hitReader = s.getReader();
//		int i = 0;
//		while(hitReader.next()){
//			i++;
//			HitElement he = hitReader.read();
//			System.out.println(i+") " +he.docNo()+" : "+he.score());
//		}
	}
	
	public void testMultiple() throws SettingException, IOException, ClauseException, IRException{
		String homePath = "testHome/";
		String collection ="test3";
		
		File seg1Dir = new File("");//IRSettings.getSegmentPath(collection, 0, 0));
		File seg2Dir = new File("");//IRSettings.getSegmentPath(collection, 0, 1));
		int segmentSize = 2;
		
		Schema schema = new Schema(null);//collection, true);
		
		Query q = new Query();
		Clause c = new Clause(new Term("title","티셔츠"), Clause.Operator.AND, new Term("title","반팔"));
		c = new Clause(c, Clause.Operator.OR, new Term("seller","michael"));
		q.setClause(c);
		//sort
		Sorts sorts = new Sorts();
		sorts.add(new Sort("price", true));
		sorts.add(new Sort("_score_", true));
		q.setSorts(sorts);
		
		//Group
		Groups groups = new Groups();
		groups.add(new Group("price", new GroupFunction[]{new GroupFunction(GroupFunctionType.COUNT, Group.SORT_VALUE_DESC, null)}, Group.SORT_KEY_ASC, 0));
		groups.add(new Group("buyers", new GroupFunction[]{new GroupFunction(GroupFunctionType.COUNT, Group.SORT_VALUE_DESC, null)}, Group.SORT_VALUE_ASC, 0));
		q.setGroups(groups);
		int groupSize = groups.size();
		
		int rows = 20;
		
		q.setMeta(new Metadata(0,rows,0,null));
		
		SegmentReader[] readers = new SegmentReader[segmentSize];
//		SegmentInfo segmentInfo1 = new SegmentInfo(0, seg1Dir);
//		SegmentInfo segmentInfo2 = new SegmentInfo(1, seg2Dir);
//		readers[0] = new SegmentReader(schema, seg1Dir, segmentInfo1.getBaseDocNo(), segmentInfo1.getDocCount());
//		readers[1] = new SegmentReader(schema, seg2Dir, segmentInfo2.getBaseDocNo(), segmentInfo2.getDocCount());
		
		FixedMinHeap<FixedHitReader> heap = new FixedMinHeap<FixedHitReader>(rows);
		GroupDataMerger frequencyMerger = new GroupDataMerger(groups, segmentSize);
		
		
		for (int i = 0; i < readers.length; i++) {
			
//			Hit hit = readers[i].search(q);
//			FixedHitReader hitReader = hit.getHitList().getReader();
//			GroupData groupFreq = hit.getGroupData();
//			
//			//posting data
//			if(hitReader.next()){
//				heap.push(hitReader);
//			}

//			//그룹결과 put
//			for (int k = 0; k < groupFreq.groupSize(); k++) {
//				frequencyMerger.put(k, groupFreq.getGroupDataReader(k));
//			}
		}
		
		
		
		//각 세그먼트의 결과들을 rankdata를 기준으로 재정렬한다.
		FixedHitQueue totalHit = new FixedHitQueue(rows);
		int n = 0;
		while(heap.size() > 0){
			FixedHitReader r = heap.peek();
			HitElement el = r.read();
			totalHit.push(el);
			n++;
			
			//결과가 만들어졌으면 일찍 끝낸다.
			if(n == rows)
				break;
			
			if(!r.next()){
				//다 읽은 것은 버린다.
				heap.pop();
			}
			heap.heapify();
		}
		
		FixedHitReader hitReader = totalHit.getReader();
		
		System.out.println("\n==== ranking ====\n");
		
		while(hitReader.next()){
			HitElement e = hitReader.read();
			System.out.println(e.docNo()+":"+e.score());
		}
//		for(int i=0;i<result.length;i++){
//			HitElement e = (HitElement)result[i];
//			System.out.println(e.docNo()+":"+e.score());
//		}
		
		GroupsData groupData = frequencyMerger.merge();
//		GroupEntryList[] groupResult = groupData.list();
		for(int i=0;i<groupData.groupSize();i++){
			GroupEntryList entryList = groupData.getGroupEntryList(i);
			GroupDataReader r = groupData.getGroupDataReader(i);
			System.out.println("\n==== group - "+ i +"==== 갯수 : "+ entryList.size() + "\n");
			while(r.next()){
				GroupEntry e = r.read();
				System.out.println("# "+e.key+">>");
				for(int k = 0; k < e.functionSize(); k++){
					System.out.println(e.groupingValue(k));
				}
					
			}
		}
		
	}
	
}
