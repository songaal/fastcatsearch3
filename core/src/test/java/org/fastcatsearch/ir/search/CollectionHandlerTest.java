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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.fastcatsearch.ir.common.IRException;
import org.fastcatsearch.ir.common.SettingException;
import org.fastcatsearch.ir.config.CollectionContext;
import org.fastcatsearch.ir.config.IndexConfig;
import org.fastcatsearch.ir.document.Document;
import org.fastcatsearch.ir.index.SegmentWriter;
import org.fastcatsearch.ir.query.Clause;
import org.fastcatsearch.ir.query.Metadata;
import org.fastcatsearch.ir.query.Query;
import org.fastcatsearch.ir.query.Result;
import org.fastcatsearch.ir.query.Sort;
import org.fastcatsearch.ir.query.Sorts;
import org.fastcatsearch.ir.query.Term;
import org.fastcatsearch.ir.settings.Schema;


public class CollectionHandlerTest extends TestCase{
	
	String homePath = "testHome/";
	String collection ="test3";
	int dataSequence = 0;
	CollectionContext collectionContext;
	public void testLoad() throws IRException, SettingException, IOException{
		IndexConfig indexConfig = null;
		File collectionDir = null;
		Schema schema = new Schema(null);
		CollectionHandler h = new CollectionHandler(collectionContext);
	}
	
	public void testSearch() throws IRException, SettingException, IOException{
		////////
		Query q = new Query();
		Clause c = new Clause(new Term("title","티셔츠"), Clause.Operator.OR, new Term("title","반팔"));
		c = new Clause(c, Clause.Operator.OR, new Term("seller","michael"));
		q.setClause(c);
		q.setMeta(new Metadata(0,100,0,null));
		
		Sorts sorts = new Sorts();
		sorts.add(new Sort("_score_", false));
		q.setSorts(sorts);
		///////
		
		IndexConfig indexConfig = null;
		File collectionDir = null;
		Schema schema = new Schema(null);
		CollectionHandler h = new CollectionHandler(collectionContext);
		h.printSegmentStatus();
		Result result = h.searcher().search(q);
		
	}
	
	public void testFullIndexing() throws IRException, SettingException, IOException{
		
		
		IndexConfig indexConfig = null;
		File collectionDir = null;
		Schema schema = new Schema(null);
		CollectionHandler h = new CollectionHandler(collectionContext);
		int segmentNumber = 0;
		System.out.println("segmentNumber = "+segmentNumber);
		{
			String source = homePath+"dbData/db.data.0";
			File sourceFile = new File(source);
			File indexDir = new File("");//IRSettings.getSegmentPath(collection, dataSequence, segmentNumber));
			SegmentWriter writer = new SegmentWriter(schema, indexDir, indexConfig);
			Document doc = null;
			writer.addDocument(doc);
			writer.close();
			h.addSegment(segmentNumber, indexDir, null);
		}
		
		h.printSegmentStatus();
	}
	
	public void testIncrementalIndexing() throws IOException, IRException, SettingException{
		Schema schema = new Schema(null);
		IndexConfig indexConfig = null;
		File collectionDir = null;
		CollectionHandler h = new CollectionHandler(collectionContext);
		int segmentNumber = h.getNextSegmentNumber();
		System.out.println("segmentNumber = "+segmentNumber);
		
		String source = homePath+"dbData/db.data.2";
		File sourceFile = new File(source);
		File indexDir = new File("");//IRSettings.getSegmentPath(collection, dataSequence, segmentNumber));
		SegmentWriter writer = new SegmentWriter(schema, indexDir, indexConfig);
		Document doc = null;
		writer.addDocument(doc);
		writer.close();

		
		h.addSegment(segmentNumber, indexDir, null);
		
		h.printSegmentStatus();
	}
}
