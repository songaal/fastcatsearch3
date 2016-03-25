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
import org.fastcatsearch.ir.search.clause.UserOperatedClause;



import junit.framework.TestCase;

public class UserOperatedClauseTest extends TestCase{
	
	public void test1() throws IOException {
		int count1 = 100;
		int[] docs1 = new int[count1];
		makeDocs(count1, docs1);
		
		UserOperatedClause c = new UserOperatedClause(count1, docs1, null);
		RankInfo docInfo = new RankInfo();
		
		int i = 0;
		while(c.next(docInfo)){
			assertEquals(docs1[i], docInfo.docNo());
//			System.out.println(docs1[i]+", " +docInfo.docNo());
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
//			System.out.println("docs["+i+"] = "+docs[i]);
			prev = docs[i];
		}
	}
}
