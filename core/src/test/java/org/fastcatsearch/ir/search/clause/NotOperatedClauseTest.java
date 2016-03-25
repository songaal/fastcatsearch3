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

package org.fastcatsearch.ir.search.clause;


import java.io.IOException;
import java.util.Random;

import org.fastcatsearch.ir.query.RankInfo;
import org.fastcatsearch.ir.search.clause.NotOperatedClause;
import org.fastcatsearch.ir.search.clause.UserOperatedClause;
import org.junit.Test;



import junit.framework.TestCase;

public class NotOperatedClauseTest {
	
	@Test
	public void testFixed() throws IOException {
		int[] docs1 = new int[]{2,5,7,9,13,18,27,31,37,45};
		int[] weight1 = new int[]{100,200,100,200,100,200,100,200,300,100};
		
		int[] docs2 = new int[]{4,5,6,7,8,45,46};
		int[] weight2 = new int[]{300,400,300,300,400,500,100};
		
		UserOperatedClause c1 = new UserOperatedClause(docs1.length, docs1, weight1);
		UserOperatedClause c2 = new UserOperatedClause(docs2.length, docs2, weight2);
		
		RankInfo docInfo = new RankInfo();
		
		NotOperatedClause notClause = new NotOperatedClause(c1, c2);
		notClause.init();
		
		int i = 0;
		while(notClause.next(docInfo)){
			System.out.println((i+1)+" : "+docInfo.docNo()+" : "+docInfo.score());
			i++;
		}
	}
	
	@Test
	public void testFixedEmptyClause2() throws IOException {
		int[] docs1 = new int[]{2,5,7,9,13,18,27,31,37,45};
		int[] weight1 = new int[]{100,200,100,200,100,200,100,200,300,100};
		
		int[] docs2 = new int[]{45};
		int[] weight2 = new int[]{100};
		
		UserOperatedClause c1 = new UserOperatedClause(docs1.length, docs1, weight1);
		UserOperatedClause c2 = new UserOperatedClause(docs2.length, docs2, weight2);
		
		RankInfo docInfo = new RankInfo();
		
		NotOperatedClause notClause = new NotOperatedClause(c1, c2);
		notClause.init();
		
		int i = 0;
		while(notClause.next(docInfo)){
			System.out.println((i+1)+" : "+docInfo.docNo()+" : "+docInfo.score());
			i++;
		}
	}
	
	@Test
	public void testRandom() throws IOException {
		int count1 = 100;
		int[] docs1 = new int[count1];
		makeDocs(count1, docs1);
		
		int count2 = 200;
		int[] docs2 = new int[count2];
		makeDocs(count2, docs2);
		
		UserOperatedClause c1 = new UserOperatedClause(count1, docs1, null);
		UserOperatedClause c2 = new UserOperatedClause(count2, docs2, null);
		
		RankInfo docInfo = new RankInfo();
		
		NotOperatedClause notClause = new NotOperatedClause(c1, c2);
		notClause.init();
		
		int i = 0;
		while(notClause.next(docInfo)){
			System.out.println((i+1)+" : "+docInfo.docNo());
			i++;
		}
	}
	
	private Random r = new Random(System.currentTimeMillis());
	
	private void makeDocs(int count, int[] docs){
		int d = 0;
		int prev = 0;
		for(int i=0; i<count;i++){
			d = r.nextInt(20);
			docs[i] = (prev + d);
			prev = docs[i];
		}
	}
}
