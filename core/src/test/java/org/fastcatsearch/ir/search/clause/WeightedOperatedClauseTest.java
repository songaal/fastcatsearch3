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
import org.fastcatsearch.ir.search.clause.AndOperatedClause;
import org.fastcatsearch.ir.search.clause.UserOperatedClause;



import junit.framework.TestCase;

public class WeightedOperatedClauseTest extends TestCase{
	
	public void testFixed() throws IOException {
		int[] docs1 = new int[]{3,5,7,9,10};
		int[] docs1Score = new int[]{2,5,7,9,10};
		int[] docs2 = new int[]{3,7,8};
		int[] docs2Score = new int[]{3,7,8};
		UserOperatedClause c1 = new UserOperatedClause(docs1.length, docs1, docs1Score);
		UserOperatedClause c2 = new UserOperatedClause(docs2.length, docs2, docs2Score);
		
		RankInfo docInfo = new RankInfo();
		
		WeightedOperatedClause weightedClause = new WeightedOperatedClause(c1, c2);
		
		int i = 0;
		while(weightedClause.next(docInfo)){
			System.out.println((i+1)+"] "+docInfo);
			i++;
		}
	}
	
	public void testEmpty() throws IOException {
		int[] docs1 = new int[]{};
		int[] docs1Score = new int[]{};
		int[] docs2 = new int[]{3,7,8};
		int[] docs2Score = new int[]{3,7,8};
		UserOperatedClause c1 = new UserOperatedClause(docs1.length, docs1, docs1Score);
		UserOperatedClause c2 = new UserOperatedClause(docs2.length, docs2, docs2Score);
		
		RankInfo docInfo = new RankInfo();
		
		WeightedOperatedClause weightedClause = new WeightedOperatedClause(c1, c2);
		
		int i = 0;
		while(weightedClause.next(docInfo)){
			System.out.println((i+1)+"] "+docInfo);
			i++;
		}
	}
	
	
	public void testRandom() throws IOException {
		int count1 = 100;
		int[] docs1 = new int[count1];
		int[] score1 = new int[count1];
		makeDocs(count1, docs1, score1);
		
		int count2 = 100;
		int[] docs2 = new int[count2];
		int[] score2 = new int[count2];
		makeDocs(count2, docs2, score2);
		
		UserOperatedClause c1 = new UserOperatedClause(count1, docs1, score1);
		UserOperatedClause c2 = new UserOperatedClause(count2, docs2, score2);
		
		RankInfo docInfo = new RankInfo();
		
		WeightedOperatedClause weightedOperatedClause = new WeightedOperatedClause(c1, c2);
		
		
		int i = 0;
		while(weightedOperatedClause.next(docInfo)){
			System.out.println((i+1)+"] "+docInfo);
			i++;
		}
	}
	
	private Random r = new Random(System.currentTimeMillis());
	
	private void makeDocs(int count, int[] docs, int[] score){
		int d = 0;
		int prev = 0;
		for(int i=0; i<count;i++){
			d = r.nextInt(20);
			docs[i] = (prev + d);
			prev = docs[i];
			score[i] = i;
		}
	}
}
