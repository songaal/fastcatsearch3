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



import junit.framework.TestCase;
import org.fastcatsearch.ir.analysis.AnalyzerPoolManager;
import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.document.DocumentReader;
import org.fastcatsearch.ir.search.SearchIndexesReader;
import org.fastcatsearch.ir.search.clause.Clause;
import org.fastcatsearch.ir.search.clause.ClauseException;
import org.fastcatsearch.ir.search.clause.OperatedClause;
import org.fastcatsearch.ir.settings.Schema;
import org.fastcatsearch.ir.settings.SchemaSetting;

import java.io.File;
import java.io.IOException;

public class QueryTest extends TestCase{
	
	String homePath = "testHome/";
	String collection ="test2";
	String target = homePath+collection+"/data";
	
	public void testClause() throws IOException, SettingException, ClauseException, IRException{
		
		SchemaSetting schemaSetting = new SchemaSetting();
		Schema schema = new Schema(schemaSetting);
		File targetDir = new File(target);
		AnalyzerPoolManager analyzerPoolManager = null;
		IndexConfig indexConfig = null;
		SearchIndexesReader reader = new SearchIndexesReader(schema, targetDir, analyzerPoolManager, 10000);
		DocumentReader docReader = new DocumentReader(schemaSetting, targetDir);
		int totalDocNum = docReader.getDocumentCount();
		System.out.println("총 문서수 ="+totalDocNum);
		
		
		Query q = new Query();
		//Clause
		Clause c = new Clause(new Term("title","티셔츠"), Clause.Operator.AND, new Term("title","반팔"));
		c = new Clause(c, Clause.Operator.OR, new Term("seller","michael"));
		q.setClause(c);
		
		//view
		ViewContainer views = new ViewContainer();
		views.add(new View("id"));
		views.add(new View("title"));
		views.add(new View("seller"));
		views.add(new View("price"));
		views.add(new View("eval"));
		q.setViews(views);
		
		int[] viewFieldIndex = new int[views.size()];
		for(int i=0;i<views.size();i++){
			viewFieldIndex[i] = schema.getFieldSequence((views.get(i).fieldId()));
		}
		//처음부터 10개의 문서를 가져옴
		Metadata meta = new Metadata(5,5,0,null);
		q.setMeta(meta);
		
		//검색엔진으로 전송후 쿼리해석시작 
		
		Clause c2 = q.getClause();
		Metadata meta2 = q.getMeta();
		int start = meta2.start();
		int rows  = meta2.rows();
		OperatedClause oc = c2.getOperatedClause(-1, reader, null);
		RankInfo docInfo = new RankInfo();
		int count = 0;
		
		while(oc.next(docInfo)){
			if(count >= start){
				int docNo = docInfo.docNo();
				System.out.println("=== "+ (count+1)+" : "+docNo+ " ===");
				Document document = docReader.readDocument(docNo);
				for(int i=0;i<viewFieldIndex.length;i++){
					int k = viewFieldIndex[i];
					System.out.println(views.get(i).fieldId()+ " : " +document.get(k));
				}
			}
			
			if(count >= start + rows - 1)
				break;
			
			count++;
		}
		
	}

}
